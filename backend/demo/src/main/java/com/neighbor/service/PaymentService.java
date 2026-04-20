package com.neighbor.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.config.AlipayConfig;
import com.neighbor.dto.PaymentDTO;
import com.neighbor.entity.Borrow;
import com.neighbor.entity.Item;
import com.neighbor.entity.Payment;
import com.neighbor.entity.User;
import com.neighbor.enums.BorrowStatus;
import com.neighbor.enums.ErrorCode;
import com.neighbor.enums.PaymentStatus;
import com.neighbor.enums.PaymentType;
import com.neighbor.enums.TransferType;
import com.neighbor.repository.BorrowRepository;
import com.neighbor.repository.PaymentRepository;
import com.neighbor.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BorrowRepository borrowRepository;
    private final UserRepository userRepository;
    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;
    private final ObjectMapper objectMapper;
    private final TransferService transferService;

    public PaymentService(PaymentRepository paymentRepository,
                          BorrowRepository borrowRepository,
                          UserRepository userRepository,
                          AlipayClient alipayClient,
                          AlipayConfig alipayConfig,
                          ObjectMapper objectMapper,
                          TransferService transferService) {
        this.paymentRepository = paymentRepository;
        this.borrowRepository = borrowRepository;
        this.userRepository = userRepository;
        this.alipayClient = alipayClient;
        this.alipayConfig = alipayConfig;
        this.objectMapper = objectMapper;
        this.transferService = transferService;
    }

    public String createPayment(Long borrowId, Long payerId) {
        log.info("[PaymentService] 创建支付订单: borrowId={}, payerId={}", borrowId, payerId);

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));

        if (!borrow.getBorrower().getId().equals(payerId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_BORROW_REQUEST);
        }

        if (borrow.getStatus() != BorrowStatus.APPROVED) {
            throw new BusinessException(ErrorCode.BORROW_STATUS_INVALID);
        }

        if (paymentRepository.existsByBorrowIdAndStatus(borrowId, PaymentStatus.PAID)) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PAID);
        }

        Payment existingPending = paymentRepository.findByBorrowIdAndStatus(borrowId, PaymentStatus.PENDING)
                .stream().findFirst().orElse(null);
        if (existingPending != null) {
            log.info("[PaymentService] 已有待支付订单: outTradeNo={}", existingPending.getOutTradeNo());
            return buildAlipayForm(existingPending);
        }

        Item item = borrow.getItem();
        long days = java.time.temporal.ChronoUnit.DAYS.between(borrow.getStartDate(), borrow.getEndDate()) + 1;
        if (days <= 0) {
            days = 1;
        }

        BigDecimal rentAmount = item.getPricePerDay().multiply(BigDecimal.valueOf(days));
        BigDecimal depositAmount = item.getDeposit();
        BigDecimal totalAmount = rentAmount.add(depositAmount);

        if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_ZERO);
        }

        PaymentType paymentType;
        if (rentAmount.compareTo(BigDecimal.ZERO) > 0 && depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            paymentType = PaymentType.RENT_AND_DEPOSIT;
        } else if (depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            paymentType = PaymentType.DEPOSIT;
        } else {
            paymentType = PaymentType.RENT;
        }

        String outTradeNo = generateOutTradeNo(borrowId);

        Payment payment = new Payment();
        payment.setOutTradeNo(outTradeNo);
        payment.setBorrow(borrow);
        payment.setPayer(borrow.getBorrower());
        payment.setPayee(borrow.getLender());
        payment.setPaymentType(paymentType);
        payment.setRentAmount(rentAmount);
        payment.setDepositAmount(depositAmount);
        payment.setTotalAmount(totalAmount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setItemSnapshot(item.getName());

        paymentRepository.save(payment);
        log.info("[PaymentService] 支付订单已创建: outTradeNo={}, totalAmount={}", outTradeNo, totalAmount);

        return buildAlipayForm(payment);
    }

    private String buildAlipayForm(Payment payment) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());

        String subject = "邻里共享-借阅支付-" + payment.getItemSnapshot();
        if (subject.length() > 128) {
            subject = subject.substring(0, 128);
        }

        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", payment.getOutTradeNo());
        bizContent.put("total_amount", payment.getTotalAmount().toPlainString());
        bizContent.put("subject", subject);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");

        try {
            request.setBizContent(objectMapper.writeValueAsString(bizContent));
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("[PaymentService] 调用支付宝失败: {}", e.getErrMsg(), e);
            throw new BusinessException(ErrorCode.PAYMENT_ALIPAY_ERROR, "支付宝下单失败: " + e.getErrMsg());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("[PaymentService] JSON序列化失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误");
        }
    }

    @Transactional
    public void handleNotify(Map<String, String> params) {
        log.info("[PaymentService] 收到支付宝异步通知: outTradeNo={}", params.get("out_trade_no"));

        try {
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            if (!signVerified) {
                log.warn("[PaymentService] 支付宝通知验签失败");
                return;
            }
        } catch (AlipayApiException e) {
            log.error("[PaymentService] 验签异常: {}", e.getErrMsg(), e);
            return;
        }

        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
            log.info("[PaymentService] 非成功状态，忽略: tradeStatus={}", tradeStatus);
            return;
        }

        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");

        Payment payment = paymentRepository.findByOutTradeNo(outTradeNo).orElse(null);
        if (payment == null) {
            log.warn("[PaymentService] 未找到支付记录: outTradeNo={}", outTradeNo);
            return;
        }

        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("[PaymentService] 支付已处理，忽略重复通知: outTradeNo={}", outTradeNo);
            return;
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setTradeNo(tradeNo);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Borrow borrow = payment.getBorrow();
        if (borrow.getStatus() == BorrowStatus.APPROVED) {
            borrow.setStatus(BorrowStatus.ACTIVE);
            borrowRepository.save(borrow);
            log.info("[PaymentService] 支付成功，借阅状态更新为ACTIVE: borrowId={}", borrow.getId());
        }

        log.info("[PaymentService] 支付成功: outTradeNo={}, tradeNo={}", outTradeNo, tradeNo);
    }

    public boolean verifyReturn(Map<String, String> params) {
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );
        } catch (AlipayApiException e) {
            log.error("[PaymentService] 同步返回验签失败: {}", e.getErrMsg(), e);
            return false;
        }
    }

    @Transactional
    public void refundDeposit(Long borrowId, Long operatorId) {
        log.info("[PaymentService] 退还押金: borrowId={}, operatorId={}", borrowId, operatorId);

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));

        if (!borrow.getLender().getId().equals(operatorId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_BORROW_REQUEST);
        }

        if (borrow.getStatus() != BorrowStatus.RETURNED) {
            throw new BusinessException(ErrorCode.BORROW_STATUS_INVALID);
        }

        List<Payment> payments = paymentRepository.findByBorrowIdAndStatus(borrowId, PaymentStatus.PAID);
        if (payments.isEmpty()) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        Payment payment = payments.get(0);

        if (payment.getDepositAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PAYMENT_NO_DEPOSIT);
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED || payment.getStatus() == PaymentStatus.REFUNDING) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_REFUNDED);
        }

        payment.setStatus(PaymentStatus.REFUNDING);
        paymentRepository.save(payment);

        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();
        Map<String, Object> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", payment.getOutTradeNo());
        bizContent.put("refund_amount", payment.getDepositAmount().toPlainString());
        bizContent.put("out_request_no", payment.getOutTradeNo() + "_refund");

        try {
            refundRequest.setBizContent(objectMapper.writeValueAsString(bizContent));
            AlipayTradeRefundResponse response = alipayClient.execute(refundRequest);
            if (response.isSuccess()) {
                payment.setStatus(PaymentStatus.REFUNDED);
                payment.setRefundAmount(payment.getDepositAmount());
                payment.setRefundTradeNo(response.getTradeNo());
                payment.setRefundedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                log.info("[PaymentService] 押金退还成功: borrowId={}, refundAmount={}", borrowId, payment.getDepositAmount());

                if (payment.getRentAmount().compareTo(BigDecimal.ZERO) > 0) {
                    try {
                        transferService.transferToLender(payment, payment.getRentAmount(), TransferType.RENT);
                        log.info("[PaymentService] 租金已转账给借出者: borrowId={}, rentAmount={}", borrowId, payment.getRentAmount());
                    } catch (Exception e) {
                        log.warn("[PaymentService] 租金转账失败，不影响押金退还: {}", e.getMessage());
                    }
                }
            } else {
                payment.setStatus(PaymentStatus.REFUND_FAILED);
                paymentRepository.save(payment);
                log.warn("[PaymentService] 押金退还失败: {}", response.getSubMsg());
                throw new BusinessException(ErrorCode.PAYMENT_REFUND_FAILED, "退款失败: " + response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            payment.setStatus(PaymentStatus.REFUND_FAILED);
            paymentRepository.save(payment);
            log.error("[PaymentService] 退款接口异常: {}", e.getErrMsg(), e);
            throw new BusinessException(ErrorCode.PAYMENT_REFUND_FAILED, "退款异常: " + e.getErrMsg());
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            payment.setStatus(PaymentStatus.REFUND_FAILED);
            paymentRepository.save(payment);
            log.error("[PaymentService] 退款JSON序列化失败: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统错误");
        }
    }

    @Transactional
    public void closePayment(Long borrowId, Long payerId) {
        log.info("[PaymentService] 关闭支付: borrowId={}, payerId={}", borrowId, payerId);

        List<Payment> pendingPayments = paymentRepository.findByBorrowIdAndStatus(borrowId, PaymentStatus.PENDING);
        for (Payment payment : pendingPayments) {
            payment.setStatus(PaymentStatus.CLOSED);
            paymentRepository.save(payment);
        }
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByBorrowId(Long borrowId) {
        List<Payment> payments = paymentRepository.findByBorrowId(borrowId);
        if (payments.isEmpty()) {
            return null;
        }
        return toDTO(payments.get(0));
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPaymentByOutTradeNo(String outTradeNo) {
        return paymentRepository.findByOutTradeNo(outTradeNo)
                .map(this::toDTO)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getMyPayments(Long userId) {
        return paymentRepository.findByPayerId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getReceivedPayments(Long userId) {
        return paymentRepository.findByPayeeId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void skipPayment(Long borrowId, Long payerId) {
        log.info("[PaymentService] 跳过支付(测试): borrowId={}, payerId={}", borrowId, payerId);

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));

        if (!borrow.getBorrower().getId().equals(payerId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_BORROW_REQUEST);
        }

        if (borrow.getStatus() != BorrowStatus.APPROVED) {
            throw new BusinessException(ErrorCode.BORROW_STATUS_INVALID);
        }

        if (paymentRepository.existsByBorrowIdAndStatus(borrowId, PaymentStatus.PAID)) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_PAID);
        }

        Item item = borrow.getItem();
        long days = java.time.temporal.ChronoUnit.DAYS.between(borrow.getStartDate(), borrow.getEndDate()) + 1;
        if (days <= 0) days = 1;

        BigDecimal rentAmount = item.getPricePerDay().multiply(BigDecimal.valueOf(days));
        BigDecimal depositAmount = item.getDeposit();
        BigDecimal totalAmount = rentAmount.add(depositAmount);

        PaymentType paymentType;
        if (rentAmount.compareTo(BigDecimal.ZERO) > 0 && depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            paymentType = PaymentType.RENT_AND_DEPOSIT;
        } else if (depositAmount.compareTo(BigDecimal.ZERO) > 0) {
            paymentType = PaymentType.DEPOSIT;
        } else {
            paymentType = PaymentType.RENT;
        }

        String outTradeNo = generateOutTradeNo(borrowId);

        Payment payment = new Payment();
        payment.setOutTradeNo(outTradeNo);
        payment.setBorrow(borrow);
        payment.setPayer(borrow.getBorrower());
        payment.setPayee(borrow.getLender());
        payment.setPaymentType(paymentType);
        payment.setRentAmount(rentAmount);
        payment.setDepositAmount(depositAmount);
        payment.setTotalAmount(totalAmount);
        payment.setStatus(PaymentStatus.PAID);
        payment.setTradeNo("SKIP_" + outTradeNo);
        payment.setPaidAt(LocalDateTime.now());
        payment.setItemSnapshot(item.getName());
        paymentRepository.save(payment);

        borrow.setStatus(BorrowStatus.ACTIVE);
        borrowRepository.save(borrow);

        log.info("[PaymentService] 跳过支付完成: borrowId={}", borrowId);
    }

    @Transactional
    public void skipRefund(Long borrowId, Long operatorId) {
        log.info("[PaymentService] 跳过退还(测试): borrowId={}, operatorId={}", borrowId, operatorId);

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BORROW_NOT_FOUND));

        if (!borrow.getLender().getId().equals(operatorId)) {
            throw new BusinessException(ErrorCode.NOT_YOUR_BORROW_REQUEST);
        }

        if (borrow.getStatus() != BorrowStatus.RETURNED) {
            throw new BusinessException(ErrorCode.BORROW_STATUS_INVALID);
        }

        List<Payment> payments = paymentRepository.findByBorrowIdAndStatus(borrowId, PaymentStatus.PAID);
        if (payments.isEmpty()) {
            throw new BusinessException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        Payment payment = payments.get(0);

        if (payment.getDepositAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PAYMENT_NO_DEPOSIT);
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED || payment.getStatus() == PaymentStatus.REFUNDING) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_REFUNDED);
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundAmount(payment.getDepositAmount());
        payment.setRefundTradeNo("SKIP_REFUND_" + payment.getOutTradeNo());
        payment.setRefundedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        if (payment.getRentAmount().compareTo(BigDecimal.ZERO) > 0) {
            try {
                transferService.transferToLender(payment, payment.getRentAmount(), TransferType.RENT);
            } catch (Exception e) {
                log.warn("[PaymentService] 跳过退还时租金转账失败: {}", e.getMessage());
            }
        }

        log.info("[PaymentService] 跳过退还完成: borrowId={}", borrowId);
    }

    private String generateOutTradeNo(Long borrowId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "NS" + timestamp + borrowId + random;
    }

    private PaymentDTO toDTO(Payment payment) {
        return new PaymentDTO(
                payment.getId(),
                payment.getOutTradeNo(),
                payment.getTradeNo(),
                payment.getBorrow().getId(),
                payment.getItemSnapshot(),
                payment.getPayer().getId(),
                payment.getPayer().getNickname(),
                payment.getPayee().getId(),
                payment.getPayee().getNickname(),
                payment.getPaymentType(),
                payment.getRentAmount(),
                payment.getDepositAmount(),
                payment.getTotalAmount(),
                payment.getStatus(),
                payment.getPaidAt(),
                payment.getRefundAmount(),
                payment.getRefundedAt(),
                payment.getDeductionAmount(),
                payment.getDeductionReason(),
                payment.getDeductionType(),
                payment.getActualRefundAmount(),
                payment.getCreatedAt()
        );
    }
}
