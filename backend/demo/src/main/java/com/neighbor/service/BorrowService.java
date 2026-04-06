package com.neighbor.service;

import com.neighbor.dto.ApproveRequest;
import com.neighbor.dto.BorrowDTO;
import com.neighbor.dto.BorrowRequest;
import com.neighbor.dto.PushNotification;
import com.neighbor.entity.Borrow;
import com.neighbor.entity.Item;
import com.neighbor.entity.User;
import com.neighbor.enums.BorrowStatus;
import com.neighbor.enums.ErrorCode;
import com.neighbor.enums.ItemStatus;
import com.neighbor.enums.MessageType;
import com.neighbor.repository.BorrowRepository;
import com.neighbor.repository.ItemRepository;
import com.neighbor.repository.UserRepository;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.websocket.WebSocketPushService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BorrowService {

    private final BorrowRepository borrowRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final WebSocketPushService webSocketPushService;

    public BorrowService(BorrowRepository borrowRepository, ItemRepository itemRepository, 
                         UserRepository userRepository, MessageService messageService,
                         WebSocketPushService webSocketPushService) {
        this.borrowRepository = borrowRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.webSocketPushService = webSocketPushService;
    }

    @Transactional(readOnly = true)
    public BorrowDTO getBorrowById(Long id) {
        Borrow borrow = borrowRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));
        return toDTO(borrow);
    }

    public Long createBorrow(BorrowRequest request, Long borrowerId) {
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
        
        if (item.getStatus() != ItemStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.ITEM_NOT_AVAILABLE);
        }
        
        User borrower = userRepository.findById(borrowerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        User lender = item.getOwner();
        
        if (borrowerId.equals(lender.getId())) {
            throw new BusinessException(ErrorCode.CANNOT_BORROW_OWN_ITEM);
        }
        
        if (request.startDate().isAfter(request.endDate())) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }
        
        if (request.startDate().isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.START_DATE_IN_PAST);
        }
        
        Borrow borrow = new Borrow();
        borrow.setItem(item);
        borrow.setBorrower(borrower);
        borrow.setLender(lender);
        borrow.setStartDate(request.startDate());
        borrow.setEndDate(request.endDate());
        borrow.setPurpose(request.purpose());
        borrow.setStatus(BorrowStatus.PENDING);
        
        Borrow saved = borrowRepository.save(borrow);
        
        messageService.sendBorrowNotification(saved, MessageType.BORROW_APPLY);
        
        Long lenderPendingCount = webSocketPushService.getPendingCount(lender.getId());
        webSocketPushService.pushToUser(lender.getId(), 
            PushNotification.newBorrowApply(item.getId(), lenderPendingCount));
        
        return saved.getId();
    }

    public void approveBorrow(Long borrowId, Long lenderId, ApproveRequest request) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));
        
        if (!borrow.getLender().getId().equals(lenderId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_BORROW_REQUEST);
        }
        
        if (borrow.getStatus() != BorrowStatus.PENDING) {
            throw new BusinessException(ErrorCode.BORROW_STATUS_INVALID);
        }
        
        Long borrowerId = borrow.getBorrower().getId();
        Long itemId = borrow.getItem().getId();
        
        if (request.approved()) {
            borrow.setStatus(BorrowStatus.APPROVED);
            borrow.getItem().setStatus(ItemStatus.BORROWED);
            messageService.sendBorrowNotification(borrow, MessageType.BORROW_APPROVED);
            
            webSocketPushService.pushToUser(borrowerId, PushNotification.requestApproved(itemId));
            
            Long lenderPendingCount = webSocketPushService.getPendingCount(lenderId);
            webSocketPushService.pushToUser(lenderId, 
                PushNotification.itemStatusChanged(itemId, "BORROWED", lenderPendingCount));
        } else {
            borrow.setStatus(BorrowStatus.REJECTED);
            borrow.setRejectReason(request.reason());
            messageService.sendBorrowNotification(borrow, MessageType.BORROW_REJECTED);
            
            webSocketPushService.pushToUser(borrowerId, PushNotification.requestRejected(itemId));
            
            Long lenderPendingCount = webSocketPushService.getPendingCount(lenderId);
            webSocketPushService.pushToUser(lenderId, 
                PushNotification.itemStatusChanged(itemId, "AVAILABLE", lenderPendingCount));
        }
        
        borrowRepository.save(borrow);
    }

    public void confirmPickup(Long borrowId, Long borrowerId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));
        
        if (!borrow.getBorrower().getId().equals(borrowerId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_BORROW_REQUEST);
        }
        
        if (borrow.getStatus() != BorrowStatus.APPROVED) {
            throw new BusinessException(ErrorCode.BORROW_STATUS_INVALID);
        }
        
        borrow.setStatus(BorrowStatus.ACTIVE);
        borrowRepository.save(borrow);
    }

    public void confirmReturn(Long borrowId, Long lenderId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));
        
        if (!borrow.getLender().getId().equals(lenderId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_BORROW_REQUEST);
        }
        
        if (borrow.getStatus() != BorrowStatus.ACTIVE && 
            borrow.getStatus() != BorrowStatus.OVERDUE && 
            borrow.getStatus() != BorrowStatus.RETURN_REQUESTED) {
            throw new BusinessException(ErrorCode.BORROW_STATUS_INVALID);
        }
        
        Long borrowerId = borrow.getBorrower().getId();
        Long itemId = borrow.getItem().getId();
        
        borrow.setStatus(BorrowStatus.RETURNED);
        borrow.setActualReturnDate(LocalDate.now());
        borrow.getItem().setStatus(ItemStatus.AVAILABLE);
        
        User borrower = borrow.getBorrower();
        borrower.setBorrowCount(borrower.getBorrowCount() + 1);
        
        User lender = borrow.getLender();
        lender.setLendCount(lender.getLendCount() + 1);
        
        borrowRepository.save(borrow);
        userRepository.save(borrower);
        userRepository.save(lender);
        
        webSocketPushService.pushToUser(borrowerId, PushNotification.borrowReturned(itemId));
        
        Long lenderPendingCount = webSocketPushService.getPendingCount(lenderId);
        webSocketPushService.pushToUser(lenderId, 
            PushNotification.returnConfirmed(itemId, lenderPendingCount));
    }

    public void remindReturn(Long borrowId, Long lenderId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));
        
        if (!borrow.getLender().getId().equals(lenderId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_BORROW_REQUEST);
        }
        
        if (borrow.getStatus() != BorrowStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.BORROW_STATUS_INVALID);
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastRemind = borrow.getLastRemindAt();
        
        if (lastRemind != null && lastRemind.toLocalDate().equals(now.toLocalDate())) {
            throw new BusinessException(ErrorCode.REMIND_LIMIT_EXCEEDED);
        }
        
        borrow.setRemindCount(borrow.getRemindCount() + 1);
        borrow.setLastRemindAt(now);
        borrowRepository.save(borrow);
    }

    public void cancelBorrow(Long borrowId, Long borrowerId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));
        
        if (!borrow.getBorrower().getId().equals(borrowerId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_BORROW_REQUEST);
        }
        
        if (borrow.getStatus() != BorrowStatus.PENDING && borrow.getStatus() != BorrowStatus.APPROVED) {
            throw new BusinessException(ErrorCode.BORROW_STATUS_INVALID);
        }
        
        borrow.setStatus(BorrowStatus.CANCELLED);
        borrowRepository.save(borrow);
    }

    public void applyReturn(Long borrowId, Long borrowerId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));
        
        if (!borrow.getBorrower().getId().equals(borrowerId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_BORROW_REQUEST);
        }
        
        if (borrow.getStatus() != BorrowStatus.ACTIVE && borrow.getStatus() != BorrowStatus.OVERDUE) {
            throw new BusinessException(ErrorCode.BORROW_STATUS_INVALID);
        }
        
        Long lenderId = borrow.getLender().getId();
        Long itemId = borrow.getItem().getId();
        
        borrow.setStatus(BorrowStatus.RETURN_REQUESTED);
        borrowRepository.save(borrow);
        
        messageService.sendBorrowNotification(borrow, MessageType.RETURN_CONFIRM);
        
        Long lenderPendingCount = webSocketPushService.getPendingCount(lenderId);
        webSocketPushService.pushToUser(lenderId, 
            PushNotification.returnRequested(itemId, lenderPendingCount));
    }

    @Transactional(readOnly = true)
    public Page<BorrowDTO> getMyBorrowed(Long borrowerId, Pageable pageable) {
        return borrowRepository.findByBorrowerId(borrowerId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public Page<BorrowDTO> getMyLent(Long lenderId, Pageable pageable) {
        return borrowRepository.findByLenderId(lenderId, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<BorrowDTO> getPendingBorrows(Long lenderId) {
        return borrowRepository.findByLenderIdAndStatus(lenderId, BorrowStatus.PENDING, Pageable.unpaged())
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private BorrowDTO toDTO(Borrow borrow) {
        BorrowDTO.ItemInfo itemInfo = new BorrowDTO.ItemInfo(
                borrow.getItem().getId(),
                borrow.getItem().getName(),
                borrow.getItem().getImages() != null && !borrow.getItem().getImages().isEmpty() 
                        ? borrow.getItem().getImages().get(0).getUrl() 
                        : null
        );
        
        BorrowDTO.UserInfo borrowerInfo = new BorrowDTO.UserInfo(
                borrow.getBorrower().getId(),
                borrow.getBorrower().getNickname(),
                borrow.getBorrower().getAvatar()
        );
        
        BorrowDTO.UserInfo lenderInfo = new BorrowDTO.UserInfo(
                borrow.getLender().getId(),
                borrow.getLender().getNickname(),
                borrow.getLender().getAvatar()
        );
        
        return new BorrowDTO(
                borrow.getId(),
                itemInfo,
                borrowerInfo,
                lenderInfo,
                borrow.getStartDate(),
                borrow.getEndDate(),
                borrow.getActualReturnDate(),
                borrow.getStatus(),
                borrow.getPurpose(),
                borrow.getRejectReason()
        );
    }
}
