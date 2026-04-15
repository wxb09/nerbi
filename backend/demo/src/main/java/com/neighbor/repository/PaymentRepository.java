package com.neighbor.repository;

import com.neighbor.entity.Payment;
import com.neighbor.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOutTradeNo(String outTradeNo);

    Optional<Payment> findByTradeNo(String tradeNo);

    List<Payment> findByBorrowId(Long borrowId);

    List<Payment> findByPayerId(Long payerId);

    List<Payment> findByPayeeId(Long payeeId);

    List<Payment> findByBorrowIdAndStatus(Long borrowId, PaymentStatus status);

    boolean existsByBorrowIdAndStatus(Long borrowId, PaymentStatus status);
}
