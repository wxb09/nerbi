package com.neighbor.service;

import com.neighbor.common.exception.BusinessException;
import com.neighbor.dto.*;
import com.neighbor.entity.*;
import com.neighbor.enums.*;
import com.neighbor.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BorrowRepository borrowRepository;
    private final DisputeRepository disputeRepository;
    private final MessageService messageService;

    public AdminService(UserRepository userRepository,
                        ItemRepository itemRepository,
                        BorrowRepository borrowRepository,
                        DisputeRepository disputeRepository,
                        MessageService messageService) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.borrowRepository = borrowRepository;
        this.disputeRepository = disputeRepository;
        this.messageService = messageService;
    }

    public AdminStatsDTO getStats() {
        LocalDateTime thisMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return new AdminStatsDTO(
                userRepository.count(),
                itemRepository.count(),
                borrowRepository.count(),
                itemRepository.countByStatus(ItemStatus.PENDING_REVIEW),
                borrowRepository.countByStatusIn(List.of(BorrowStatus.ACTIVE, BorrowStatus.APPROVED, BorrowStatus.OVERDUE)),
                disputeRepository.countByStatus(DisputeStatus.PENDING),
                userRepository.countByStatus(UserStatus.BANNED),
                userRepository.countByCreatedAtAfter(thisMonth),
                borrowRepository.countByCreatedAtAfter(thisMonth)
        );
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDTO> getUsers(String keyword, UserStatus status, UserRole role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.searchUsers(keyword, status, role, pageable);
        return users.map(this::toAdminUserDTO);
    }

    @Transactional
    public void banUser(Long userId, Long operatorId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(4003, "用户不存在"));
        if (user.getStatus() == UserStatus.BANNED) {
            throw new BusinessException(2001, "用户已被封禁");
        }
        if (user.getRole() == UserRole.ADMIN) {
            throw new BusinessException(1002, "不能封禁管理员");
        }
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);

        List<Item> availableItems = itemRepository.findByOwnerIdAndStatus(userId, ItemStatus.AVAILABLE, Pageable.unpaged()).getContent();
        for (Item item : availableItems) {
            item.setStatus(ItemStatus.OFFLINE);
            itemRepository.save(item);
        }

        messageService.sendMessage(userId, MessageType.SYSTEM,
                "账号已被封禁", "您的账号因违规操作已被管理员封禁，如有疑问请联系管理员。", null);
    }

    @Transactional
    public void unbanUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(4003, "用户不存在"));
        if (user.getStatus() != UserStatus.BANNED) {
            throw new BusinessException(2001, "用户未被封禁");
        }
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        messageService.sendMessage(userId, MessageType.SYSTEM,
                "账号已解封", "您的账号已被管理员解封，可以正常使用。", null);
    }

    @Transactional(readOnly = true)
    public Page<AdminItemDTO> getPendingReviewItems(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Item> items = itemRepository.findByStatus(ItemStatus.PENDING_REVIEW, pageable);
        return items.map(this::toAdminItemDTO);
    }

    @Transactional(readOnly = true)
    public Page<AdminItemDTO> getAllItems(ItemStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Item> items;
        if (status != null) {
            items = itemRepository.findByStatus(status, pageable);
        } else {
            items = itemRepository.findAll(pageable);
        }
        return items.map(this::toAdminItemDTO);
    }

    @Transactional
    public void auditItem(Long itemId, AuditItemRequest request, Long operatorId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(3001, "物品不存在"));
        if (item.getStatus() != ItemStatus.PENDING_REVIEW) {
            throw new BusinessException(2001, "物品不在待审核状态");
        }

        item.setAuditedAt(LocalDateTime.now());
        item.setAuditRemark(request.remark());

        if ("approve".equals(request.action())) {
            item.setStatus(ItemStatus.AVAILABLE);
            item.setPublishedAt(LocalDateTime.now());
            messageService.sendMessage(item.getOwner().getId(), MessageType.SYSTEM,
                    "物品审核通过", "您发布的物品「" + item.getName() + "」已通过审核，现在可以正常借阅。", item.getId());
        } else if ("reject".equals(request.action())) {
            item.setStatus(ItemStatus.DRAFT);
            messageService.sendMessage(item.getOwner().getId(), MessageType.SYSTEM,
                    "物品审核未通过", "您发布的物品「" + item.getName() + "」未通过审核" +
                    (request.remark() != null ? "，原因：" + request.remark() : "") + "，请修改后重新提交。", item.getId());
        } else {
            throw new BusinessException(2001, "无效的审核操作");
        }
        itemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public Page<AdminBorrowDTO> getBorrows(BorrowStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Borrow> borrows;
        if (status != null) {
            borrows = borrowRepository.findByStatus(status, pageable);
        } else {
            borrows = borrowRepository.findAll(pageable);
        }
        return borrows.map(this::toAdminBorrowDTO);
    }

    @Transactional
    public Dispute createDispute(Long borrowId, Long reporterId, String reason) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new BusinessException(4001, "借阅记录不存在"));

        if (disputeRepository.existsByBorrowIdAndStatusIn(borrowId,
                List.of(DisputeStatus.PENDING, DisputeStatus.INVESTIGATING))) {
            throw new BusinessException(2001, "该借阅已有进行中的纠纷");
        }

        if (borrow.getStatus() == BorrowStatus.DISPUTED) {
            throw new BusinessException(2001, "该借阅已处于纠纷状态");
        }

        Dispute dispute = new Dispute();
        dispute.setBorrow(borrow);
        dispute.setReporter(userRepository.getReferenceById(reporterId));
        dispute.setReason(reason);
        dispute = disputeRepository.save(dispute);

        borrow.setStatus(BorrowStatus.DISPUTED);
        borrowRepository.save(borrow);

        return dispute;
    }

    @Transactional(readOnly = true)
    public Page<DisputeDTO> getDisputes(DisputeStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Dispute> disputes = disputeRepository.searchDisputes(status, pageable);
        return disputes.map(this::toDisputeDTO);
    }

    @Transactional
    public void resolveDispute(Long disputeId, ResolveDisputeRequest request, Long operatorId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(2001, "纠纷记录不存在"));
        if (dispute.getStatus() == DisputeStatus.RESOLVED || dispute.getStatus() == DisputeStatus.DISMISSED) {
            throw new BusinessException(2001, "纠纷已处理");
        }

        User operator = userRepository.getReferenceById(operatorId);
        dispute.setResolvedBy(operator);
        dispute.setResolvedAt(LocalDateTime.now());
        dispute.setResolution(request.resolution());

        Borrow borrow = dispute.getBorrow();

        if ("resolve".equals(request.action())) {
            dispute.setStatus(DisputeStatus.RESOLVED);
            borrow.setStatus(BorrowStatus.RETURNED);
            if (borrow.getItem() != null) {
                borrow.getItem().setStatus(ItemStatus.AVAILABLE);
            }
        } else if ("dismiss".equals(request.action())) {
            dispute.setStatus(DisputeStatus.DISMISSED);
            borrow.setStatus(BorrowStatus.ACTIVE);
        } else {
            throw new BusinessException(2001, "无效的处理操作");
        }

        disputeRepository.save(dispute);
        borrowRepository.save(borrow);

        messageService.sendMessage(borrow.getBorrower().getId(), MessageType.SYSTEM,
                "纠纷处理通知", "您提交的借阅纠纷已处理" +
                (request.resolution() != null ? "：" + request.resolution() : "") + "。", borrow.getId());
        messageService.sendMessage(borrow.getLender().getId(), MessageType.SYSTEM,
                "纠纷处理通知", "您涉及的借阅纠纷已处理" +
                (request.resolution() != null ? "：" + request.resolution() : "") + "。", borrow.getId());
    }

    private AdminUserDTO toAdminUserDTO(User user) {
        return new AdminUserDTO(
                user.getId(),
                user.getNickname(),
                user.getAvatar(),
                user.getPhone(),
                user.getCommunity() != null ? user.getCommunity().getName() : null,
                user.getBuilding(),
                user.getCreditScore(),
                user.getBorrowCount(),
                user.getLendCount(),
                user.getCo2Saved(),
                user.getStatus().name(),
                user.getRole().name(),
                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
    }

    private AdminItemDTO toAdminItemDTO(Item item) {
        String mainImage = item.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsMain()))
                .map(ItemImage::getUrl)
                .findFirst()
                .orElse(item.getImages().stream().findFirst().map(ItemImage::getUrl).orElse(null));
        return new AdminItemDTO(
                item.getId(),
                item.getName(),
                mainImage,
                item.getStatus(),
                item.getCategory() != null ? item.getCategory().getName() : null,
                item.getPricePerDay(),
                item.getOwner() != null ? item.getOwner().getNickname() : null,
                item.getOwner() != null ? item.getOwner().getId() : null,
                item.getAuditRemark(),
                item.getCreatedAt() != null ? item.getCreatedAt().toString() : null
        );
    }

    private AdminBorrowDTO toAdminBorrowDTO(Borrow borrow) {
        return new AdminBorrowDTO(
                borrow.getId(),
                borrow.getItem() != null ? borrow.getItem().getName() : null,
                borrow.getItem() != null ? borrow.getItem().getId() : null,
                borrow.getBorrower() != null ? borrow.getBorrower().getNickname() : null,
                borrow.getBorrower() != null ? borrow.getBorrower().getId() : null,
                borrow.getLender() != null ? borrow.getLender().getNickname() : null,
                borrow.getLender() != null ? borrow.getLender().getId() : null,
                borrow.getStartDate() != null ? borrow.getStartDate().toString() : null,
                borrow.getEndDate() != null ? borrow.getEndDate().toString() : null,
                borrow.getStatus(),
                borrow.getPurpose(),
                borrow.getCreatedAt() != null ? borrow.getCreatedAt().toString() : null
        );
    }

    private DisputeDTO toDisputeDTO(Dispute dispute) {
        return new DisputeDTO(
                dispute.getId(),
                dispute.getBorrow() != null ? dispute.getBorrow().getId() : null,
                dispute.getBorrow() != null && dispute.getBorrow().getItem() != null
                        ? dispute.getBorrow().getItem().getName() : null,
                dispute.getReporter() != null ? dispute.getReporter().getNickname() : null,
                dispute.getReporter() != null ? dispute.getReporter().getId() : null,
                dispute.getReason(),
                dispute.getStatus(),
                dispute.getResolution(),
                dispute.getResolvedBy() != null ? dispute.getResolvedBy().getNickname() : null,
                dispute.getCreatedAt() != null ? dispute.getCreatedAt().toString() : null,
                dispute.getResolvedAt() != null ? dispute.getResolvedAt().toString() : null
        );
    }
}
