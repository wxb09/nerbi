package com.neighbor.controller.api;

import com.neighbor.auth.AuthUser;
import com.neighbor.common.api.ApiResponse;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.dto.CreateDisputeRequest;
import com.neighbor.entity.Borrow;
import com.neighbor.entity.Dispute;
import com.neighbor.entity.User;
import com.neighbor.enums.BorrowStatus;
import com.neighbor.enums.DisputeStatus;
import com.neighbor.enums.MessageType;
import com.neighbor.repository.BorrowRepository;
import com.neighbor.repository.DisputeRepository;
import com.neighbor.repository.UserRepository;
import com.neighbor.service.MessageService;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/disputes")
public class DisputeController {

    private final DisputeRepository disputeRepository;
    private final BorrowRepository borrowRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public DisputeController(DisputeRepository disputeRepository,
                             BorrowRepository borrowRepository,
                             UserRepository userRepository,
                             MessageService messageService) {
        this.disputeRepository = disputeRepository;
        this.borrowRepository = borrowRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
    }

    @PostMapping
    @Transactional
    public ApiResponse<Void> createDispute(
            Authentication authentication,
            @RequestBody CreateDisputeRequest request) {
        
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser authUser)) {
            throw new BusinessException(1001, "未登录");
        }

        Long userId = Long.parseLong(authUser.userId());
        User user = userRepository.getReferenceById(userId);

        Borrow borrow = borrowRepository.findByIdWithDetails(request.borrowId())
                .orElseThrow(() -> new BusinessException(4001, "借阅记录不存在"));

        if (!userId.equals(borrow.getBorrower().getId()) && !userId.equals(borrow.getLender().getId())) {
            throw new BusinessException(1002, "您不是该借阅的参与者");
        }

        if (disputeRepository.existsByBorrowIdAndStatusIn(request.borrowId(),
                List.of(DisputeStatus.PENDING, DisputeStatus.INVESTIGATING))) {
            throw new BusinessException(2001, "该借阅已有进行中的申诉");
        }

        if (borrow.getStatus() == BorrowStatus.DISPUTED) {
            throw new BusinessException(2001, "该借阅已处于申诉状态");
        }

        Dispute dispute = new Dispute();
        dispute.setBorrow(borrow);
        dispute.setReporter(user);
        dispute.setReason(request.reason());
        dispute.setStatus(DisputeStatus.PENDING);
        disputeRepository.save(dispute);

        borrow.setStatus(BorrowStatus.DISPUTED);
        borrowRepository.save(borrow);

        Long otherUserId = userId.equals(borrow.getBorrower().getId()) 
                ? borrow.getLender().getId() 
                : borrow.getBorrower().getId();
        messageService.sendMessage(otherUserId, 
                MessageType.SYSTEM,
                "借阅申诉通知", 
                "您涉及的借阅「" + borrow.getItem().getName() + "」已被对方发起申诉，请等待管理员处理。", 
                borrow.getId());

        return ApiResponse.ok();
    }
}
