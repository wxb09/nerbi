package com.neighbor.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.dto.CreateDepositDisputeRequest;
import com.neighbor.dto.DepositDisputeDTO;
import com.neighbor.dto.ResolveDepositDisputeRequest;
import com.neighbor.entity.*;
import com.neighbor.enums.*;
import com.neighbor.repository.DepositDisputeRepository;
import com.neighbor.repository.PaymentRepository;
import com.neighbor.repository.BorrowRepository;
import com.neighbor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class DepositDisputeService {

    private static final Logger log = LoggerFactory.getLogger(DepositDisputeService.class);

    private final DepositDisputeRepository depositDisputeRepository;
    private final PaymentRepository paymentRepository;
    private final BorrowRepository borrowRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final AlipayClient alipayClient;
    private final ObjectMapper objectMapper;

    public DepositDisputeService(DepositDisputeRepository depositDisputeRepository,
                                  PaymentRepository paymentRepository,
                                  BorrowRepository borrowRepository,
                                  UserRepository userRepository,
                                  MessageService messageService,
                                  AlipayClient alipayClient,
                                  ObjectMapper objectMapper) {
        this.depositDisputeRepository = depositDisputeRepository;
        this.paymentRepository = paymentRepository;
        this.borrowRepository = borrowRepository;
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.alipayClient = alipayClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DepositDisputeDTO createDispute(CreateDepositDisputeRequest request, Long userId) {
        log.info("[DepositDisputeService] 创建押金纠纷: borrowId={}, userId={}", request.borrowId(), userId);

        Borrow borrow = borrowRepository.findById(request.borrowId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));

        boolean isLender = borrow.getLender().getId().equals(userId);
        boolean isBorrower = borrow.getBorrower().getId().equals(userId);
        if (!isLender && !isBorrower) {
            throw new BusinessException(ErrorCode.DEPOSIT_DISPUTE_NO_PERMISSION);
        }

        List<Payment> payments = paymentRepository.findByBorrowIdAndStatus(request.borrowId(), PaymentStatus.PAID);
        if (payments.isEmpty()) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }
        Payment payment = payments.get(0);

        if (payment.getDepositAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PAYMENT_NO_DEPOSIT);
        }

        if (request.claimAmount().compareTo(payment.getDepositAmount()) > 0) {
            throw new BusinessException(ErrorCode.DEPOSIT_DISPUTE_CLAIM_EXCEED);
        }

        boolean hasActiveDispute = depositDisputeRepository.existsByBorrowIdAndStatusIn(
                request.borrowId(),
                List.of(DepositDisputeStatus.PENDING, DepositDisputeStatus.PROCESSING));
        if (hasActiveDispute) {
            throw new BusinessException(ErrorCode.DEPOSIT_DISPUTE_ALREADY_EXISTS);
        }

        DepositDispute dispute = new DepositDispute();
        dispute.setPayment(payment);
        dispute.setBorrow(borrow);
        dispute.setInitiator(userRepository.getReferenceById(userId));
        dispute.setInitiatorType(isLender ? InitiatorType.LENDER : InitiatorType.BORROWER);
        dispute.setDisputeType(request.disputeType());
        dispute.setDescription(request.description());
        dispute.setEvidenceImages(request.evidenceImages());
        dispute.setClaimAmount(request.claimAmount());
        dispute.setClaimReason(request.claimReason());
        dispute.setStatus(DepositDisputeStatus.PENDING);

        dispute = depositDisputeRepository.save(dispute);

        payment.setStatus(PaymentStatus.DISPUTED);
        paymentRepository.save(payment);

        Long otherUserId = isLender ? borrow.getBorrower().getId() : borrow.getLender().getId();
        String initiatorName = isLender ? "借出者" : "借入者";
        messageService.sendMessage(otherUserId, MessageType.SYSTEM,
                "押金纠纷通知",
                initiatorName + "对借阅订单「" + borrow.getItem().getName() + "」发起了押金纠纷，请关注处理进度。",
                borrow.getId());

        log.info("[DepositDisputeService] 押金纠纷创建成功: id={}", dispute.getId());
        return toDTO(dispute);
    }

    @Transactional(readOnly = true)
    public DepositDisputeDTO getDispute(Long disputeId, Long userId) {
        DepositDispute dispute = depositDisputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPOSIT_DISPUTE_NOT_FOUND));
        return toDTO(dispute);
    }

    @Transactional(readOnly = true)
    public Page<DepositDisputeDTO> getMyDisputes(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return depositDisputeRepository.findByRelatedUser(userId, pageable)
                .map(this::toDTO);
    }

    @Transactional
    public void cancelDispute(Long disputeId, Long userId) {
        log.info("[DepositDisputeService] 撤销押金纠纷: disputeId={}, userId={}", disputeId, userId);

        DepositDispute dispute = depositDisputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPOSIT_DISPUTE_NOT_FOUND));

        if (!dispute.getInitiator().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.DEPOSIT_DISPUTE_NO_PERMISSION);
        }

        if (dispute.getStatus() != DepositDisputeStatus.PENDING) {
            throw new BusinessException(ErrorCode.DEPOSIT_DISPUTE_ALREADY_HANDLED);
        }

        dispute.setStatus(DepositDisputeStatus.CANCELLED);
        depositDisputeRepository.save(dispute);

        Payment payment = dispute.getPayment();
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        Borrow borrow = dispute.getBorrow();
        Long otherUserId = dispute.getInitiatorType() == InitiatorType.LENDER
                ? borrow.getBorrower().getId() : borrow.getLender().getId();
        messageService.sendMessage(otherUserId, MessageType.SYSTEM,
                "押金纠纷已撤销",
                "对方已撤销对借阅订单「" + borrow.getItem().getName() + "」的押金纠纷申请。",
                borrow.getId());

        log.info("[DepositDisputeService] 押金纠纷已撤销: disputeId={}", disputeId);
    }

    @Transactional(readOnly = true)
    public Page<DepositDisputeDTO> getAdminDisputes(DepositDisputeStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return depositDisputeRepository.searchDisputes(status, pageable)
                .map(this::toDTO);
    }

    @Transactional
    public DepositDisputeDTO resolveDispute(Long disputeId, ResolveDepositDisputeRequest request, Long adminId) {
        log.info("[DepositDisputeService] 管理员处理押金纠纷: disputeId={}, action={}, adminId={}",
                disputeId, request.action(), adminId);

        DepositDispute dispute = depositDisputeRepository.findById(disputeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPOSIT_DISPUTE_NOT_FOUND));

        if (dispute.getStatus() == DepositDisputeStatus.APPROVED
                || dispute.getStatus() == DepositDisputeStatus.REJECTED
                || dispute.getStatus() == DepositDisputeStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.DEPOSIT_DISPUTE_ALREADY_HANDLED);
        }

        Payment payment = dispute.getPayment();
        Borrow borrow = dispute.getBorrow();

        switch (request.action()) {
            case "investigate" -> {
                dispute.setStatus(DepositDisputeStatus.PROCESSING);
                dispute.setHandler(userRepository.getReferenceById(adminId));
                messageService.sendMessage(borrow.getBorrower().getId(), MessageType.SYSTEM,
                        "押金纠纷处理中",
                        "您涉及的借阅订单「" + borrow.getItem().getName() + "」的押金纠纷正在调查中，请耐心等待。",
                        borrow.getId());
                messageService.sendMessage(borrow.getLender().getId(), MessageType.SYSTEM,
                        "押金纠纷处理中",
                        "您涉及的借阅订单「" + borrow.getItem().getName() + "」的押金纠纷正在调查中，请耐心等待。",
                        borrow.getId());
            }
            case "approve" -> {
                BigDecimal deduction = request.actualDeduction();
                if (deduction == null || deduction.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BusinessException(ErrorCode.DEPOSIT_DISPUTE_INVALID_ACTION);
                }
                if (deduction.compareTo(payment.getDepositAmount()) > 0) {
                    throw new BusinessException(ErrorCode.DEPOSIT_DISPUTE_DEDUCTION_EXCEED);
                }

                dispute.setStatus(DepositDisputeStatus.APPROVED);
                dispute.setActualDeduction(deduction);
                dispute.setResolution(request.resolution());
                dispute.setHandler(userRepository.getReferenceById(adminId));
                dispute.setHandledAt(LocalDateTime.now());

                payment.setDeductionAmount(deduction);
                payment.setDeductionReason(request.resolution());
                payment.setDeductionType(dispute.getDisputeType());
                payment.setDeductionEvidence(dispute.getEvidenceImages());

                BigDecimal actualRefund = payment.getDepositAmount().subtract(deduction);

                if (actualRefund.compareTo(BigDecimal.ZERO) > 0) {
                    payment.setStatus(PaymentStatus.DEDUCTING);
                    paymentRepository.save(payment);
                    depositDisputeRepository.save(dispute);

                    boolean refundSuccess = processPartialRefund(payment, actualRefund);
                    if (refundSuccess) {
                        payment.setStatus(PaymentStatus.PARTIAL_REFUNDED);
                        payment.setActualRefundAmount(actualRefund);
                        payment.setRefundAmount(actualRefund);
                        payment.setRefundedAt(LocalDateTime.now());
                    } else {
                        payment.setStatus(PaymentStatus.REFUND_FAILED);
                    }
                    paymentRepository.save(payment);
                } else {
                    payment.setStatus(PaymentStatus.FULLY_DEDUCTED);
                    payment.setActualRefundAmount(BigDecimal.ZERO);
                    paymentRepository.save(payment);
                }

                String deductionMsg = "扣款" + deduction.toPlainString() + "元";
                String refundMsg = actualRefund.compareTo(BigDecimal.ZERO) > 0
                        ? "，剩余押金" + actualRefund.toPlainString() + "元已退还" : "";

                messageService.sendMessage(borrow.getBorrower().getId(), MessageType.SYSTEM,
                        "押金纠纷处理结果",
                        "借阅订单「" + borrow.getItem().getName() + "」的押金纠纷已处理：" + deductionMsg + refundMsg +
                                (request.resolution() != null ? "。" + request.resolution() : ""),
                        borrow.getId());
                messageService.sendMessage(borrow.getLender().getId(), MessageType.SYSTEM,
                        "押金纠纷处理结果",
                        "借阅订单「" + borrow.getItem().getName() + "」的押金纠纷已处理：" + deductionMsg + refundMsg +
                                (request.resolution() != null ? "。" + request.resolution() : ""),
                        borrow.getId());
            }
            case "reject" -> {
                dispute.setStatus(DepositDisputeStatus.REJECTED);
                dispute.setResolution(request.resolution());
                dispute.setHandler(userRepository.getReferenceById(adminId));
                dispute.setHandledAt(LocalDateTime.now());

                payment.setStatus(PaymentStatus.REFUNDING);
                paymentRepository.save(payment);
                depositDisputeRepository.save(dispute);

                boolean refundSuccess = processFullRefund(payment);
                if (refundSuccess) {
                    payment.setStatus(PaymentStatus.REFUNDED);
                    payment.setRefundAmount(payment.getDepositAmount());
                    payment.setActualRefundAmount(payment.getDepositAmount());
                    payment.setRefundedAt(LocalDateTime.now());
                } else {
                    payment.setStatus(PaymentStatus.REFUND_FAILED);
                }
                paymentRepository.save(payment);

                messageService.sendMessage(borrow.getBorrower().getId(), MessageType.SYSTEM,
                        "押金纠纷处理结果",
                        "借阅订单「" + borrow.getItem().getName() + "」的押金纠纷已驳回，押金将全额退还" +
                                (request.resolution() != null ? "。" + request.resolution() : ""),
                        borrow.getId());
                messageService.sendMessage(borrow.getLender().getId(), MessageType.SYSTEM,
                        "押金纠纷处理结果",
                        "借阅订单「" + borrow.getItem().getName() + "」的押金纠纷已驳回，押金将全额退还给借入者" +
                                (request.resolution() != null ? "。" + request.resolution() : ""),
                        borrow.getId());
            }
            default -> throw new BusinessException(ErrorCode.DEPOSIT_DISPUTE_INVALID_ACTION);
        }

        depositDisputeRepository.save(dispute);
        log.info("[DepositDisputeService] 押金纠纷处理完成: disputeId={}, status={}", disputeId, dispute.getStatus());
        return toDTO(dispute);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDisputeStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingCount", depositDisputeRepository.countByStatus(DepositDisputeStatus.PENDING));
        stats.put("processingCount", depositDisputeRepository.countByStatus(DepositDisputeStatus.PROCESSING));
        stats.put("approvedCount", depositDisputeRepository.countByStatus(DepositDisputeStatus.APPROVED));
        stats.put("rejectedCount", depositDisputeRepository.countByStatus(DepositDisputeStatus.REJECTED));
        return stats;
    }

    private boolean processPartialRefund(Payment payment, BigDecimal refundAmount) {
        log.info("[DepositDisputeService] 执行部分退款: outTradeNo={}, refundAmount={}",
                payment.getOutTradeNo(), refundAmount);

        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", payment.getOutTradeNo());
        bizContent.put("refund_amount", refundAmount.toPlainString());
        bizContent.put("out_request_no", payment.getOutTradeNo() + "_partial_" + System.currentTimeMillis());

        try {
            refundRequest.setBizContent(objectMapper.writeValueAsString(bizContent));
            AlipayTradeRefundResponse response = alipayClient.execute(refundRequest);
            if (response.isSuccess()) {
                payment.setRefundTradeNo(response.getTradeNo());
                log.info("[DepositDisputeService] 部分退款成功: refundAmount={}", refundAmount);
                return true;
            } else {
                log.warn("[DepositDisputeService] 部分退款失败: {}", response.getSubMsg());
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("[DepositDisputeService] 部分退款接口异常: {}", e.getErrMsg(), e);
            return false;
        } catch (JsonProcessingException e) {
            log.error("[DepositDisputeService] 部分退款JSON序列化失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean processFullRefund(Payment payment) {
        log.info("[DepositDisputeService] 执行全额退款: outTradeNo={}, depositAmount={}",
                payment.getOutTradeNo(), payment.getDepositAmount());

        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", payment.getOutTradeNo());
        bizContent.put("refund_amount", payment.getDepositAmount().toPlainString());
        bizContent.put("out_request_no", payment.getOutTradeNo() + "_full_" + System.currentTimeMillis());

        try {
            refundRequest.setBizContent(objectMapper.writeValueAsString(bizContent));
            AlipayTradeRefundResponse response = alipayClient.execute(refundRequest);
            if (response.isSuccess()) {
                payment.setRefundTradeNo(response.getTradeNo());
                log.info("[DepositDisputeService] 全额退款成功: depositAmount={}", payment.getDepositAmount());
                return true;
            } else {
                log.warn("[DepositDisputeService] 全额退款失败: {}", response.getSubMsg());
                return false;
            }
        } catch (AlipayApiException e) {
            log.error("[DepositDisputeService] 全额退款接口异常: {}", e.getErrMsg(), e);
            return false;
        } catch (JsonProcessingException e) {
            log.error("[DepositDisputeService] 全额退款JSON序列化失败: {}", e.getMessage(), e);
            return false;
        }
    }

    private DepositDisputeDTO toDTO(DepositDispute dispute) {
        return new DepositDisputeDTO(
                dispute.getId(),
                dispute.getPayment().getId(),
                dispute.getBorrow().getId(),
                dispute.getBorrow().getItem() != null ? dispute.getBorrow().getItem().getName() : null,
                dispute.getInitiator().getId(),
                dispute.getInitiator().getNickname(),
                dispute.getInitiatorType(),
                dispute.getDisputeType(),
                dispute.getDescription(),
                dispute.getEvidenceImages(),
                dispute.getClaimAmount(),
                dispute.getClaimReason(),
                dispute.getStatus(),
                dispute.getActualDeduction(),
                dispute.getResolution(),
                dispute.getHandler() != null ? dispute.getHandler().getId() : null,
                dispute.getHandler() != null ? dispute.getHandler().getNickname() : null,
                dispute.getHandledAt(),
                dispute.getPayment().getDepositAmount(),
                dispute.getCreatedAt()
        );
    }
}
