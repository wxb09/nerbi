package com.neighbor.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayFundTransUniTransferModel;
import com.alipay.api.domain.Participant;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.entity.Payment;
import com.neighbor.entity.TransferRecord;
import com.neighbor.entity.User;
import com.neighbor.enums.ErrorCode;
import com.neighbor.enums.TransferStatus;
import com.neighbor.enums.TransferType;
import com.neighbor.repository.TransferRecordRepository;
import com.neighbor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final TransferRecordRepository transferRecordRepository;
    private final UserRepository userRepository;
    private final AlipayClient alipayClient;

    public TransferService(TransferRecordRepository transferRecordRepository,
                           UserRepository userRepository,
                           AlipayClient alipayClient) {
        this.transferRecordRepository = transferRecordRepository;
        this.userRepository = userRepository;
        this.alipayClient = alipayClient;
    }

    @Transactional
    public TransferRecord transferToLender(Payment payment, BigDecimal amount, TransferType transferType) {
        User lender = payment.getPayee();

        if (lender.getAlipayAccount() == null || lender.getAlipayAccount().isBlank()) {
            log.warn("[TransferService] 借出者未绑定支付宝账号: userId={}", lender.getId());
            TransferRecord record = createRecord(payment, lender, amount, transferType);
            record.setStatus(TransferStatus.FAILED);
            record.setFailReason("借出者未绑定支付宝账号");
            return transferRecordRepository.save(record);
        }

        if (transferRecordRepository.existsByBorrowIdAndTransferTypeAndStatus(
                payment.getBorrow().getId(), transferType, TransferStatus.SUCCESS)) {
            log.info("[TransferService] 该借阅已成功转账: borrowId={}, type={}", payment.getBorrow().getId(), transferType);
            return null;
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("[TransferService] 转账金额为零，跳过: borrowId={}", payment.getBorrow().getId());
            return null;
        }

        TransferRecord record = createRecord(payment, lender, amount, transferType);

        try {
            AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
            AlipayFundTransUniTransferModel model = new AlipayFundTransUniTransferModel();

            model.setOutBizNo(record.getOutBizNo());
            model.setTransAmount(amount.toPlainString());
            model.setProductCode("TRANS_ACCOUNT_NO_PWD");
            model.setBizScene("DIRECT_TRANSFER");
            model.setOrderTitle(transferType == TransferType.RENT ? "邻里共享-租金结算" : "邻里共享-扣款结算");

            Participant payeeInfo = new Participant();
            payeeInfo.setIdentity(lender.getAlipayAccount());
            payeeInfo.setIdentityType("ALIPAY_LOGON_ID");
            payeeInfo.setName(lender.getNickname());
            model.setPayeeInfo(payeeInfo);

            model.setRemark(transferType == TransferType.RENT
                    ? "物品「" + payment.getItemSnapshot() + "」租金结算"
                    : "物品「" + payment.getItemSnapshot() + "」押金扣款结算");

            request.setBizModel(model);

            AlipayFundTransUniTransferResponse response = alipayClient.execute(request);

            if (response.isSuccess()) {
                record.setStatus(TransferStatus.SUCCESS);
                record.setAlipayOrderId(response.getOrderId());
                log.info("[TransferService] 转账成功: outBizNo={}, orderId={}, amount={}",
                        record.getOutBizNo(), response.getOrderId(), amount);
            } else {
                record.setStatus(TransferStatus.FAILED);
                record.setFailReason(response.getSubMsg());
                log.warn("[TransferService] 转账失败: outBizNo={}, reason={}",
                        record.getOutBizNo(), response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            record.setStatus(TransferStatus.FAILED);
            record.setFailReason(e.getErrMsg());
            log.error("[TransferService] 转账接口异常: {}", e.getErrMsg(), e);
        }

        return transferRecordRepository.save(record);
    }

    private TransferRecord createRecord(Payment payment, User lender, BigDecimal amount, TransferType transferType) {
        TransferRecord record = new TransferRecord();
        record.setPayment(payment);
        record.setBorrow(payment.getBorrow());
        record.setToUser(lender);
        record.setTransferType(transferType);
        record.setAmount(amount);
        record.setOutBizNo(generateOutBizNo(payment.getBorrow().getId(), transferType));
        record.setPayeeAccount(lender.getAlipayAccount());
        record.setStatus(TransferStatus.PENDING);
        return transferRecordRepository.save(record);
    }

    private String generateOutBizNo(Long borrowId, TransferType transferType) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        String prefix = transferType == TransferType.RENT ? "TR" : "TD";
        return prefix + timestamp + borrowId + random;
    }

    @Transactional(readOnly = true)
    public List<TransferRecord> getTransferByBorrowId(Long borrowId) {
        return transferRecordRepository.findByBorrowId(borrowId);
    }

    @Transactional(readOnly = true)
    public List<TransferRecord> getTransferByUserId(Long userId) {
        return transferRecordRepository.findByToUserId(userId);
    }
}
