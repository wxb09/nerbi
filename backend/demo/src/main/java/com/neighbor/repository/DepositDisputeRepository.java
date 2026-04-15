package com.neighbor.repository;

import com.neighbor.entity.DepositDispute;
import com.neighbor.enums.DepositDisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepositDisputeRepository extends JpaRepository<DepositDispute, Long> {

    Page<DepositDispute> findByStatus(DepositDisputeStatus status, Pageable pageable);

    Page<DepositDispute> findByInitiatorId(Long initiatorId, Pageable pageable);

    List<DepositDispute> findByBorrowId(Long borrowId);

    boolean existsByBorrowIdAndStatusIn(Long borrowId, List<DepositDisputeStatus> statuses);

    Long countByStatus(DepositDisputeStatus status);

    @Query("SELECT dd FROM DepositDispute dd WHERE " +
           "(:status IS NULL OR dd.status = :status)")
    Page<DepositDispute> searchDisputes(@Param("status") DepositDisputeStatus status, Pageable pageable);

    @Query("SELECT dd FROM DepositDispute dd WHERE " +
           "(dd.initiator.id = :userId OR dd.borrow.lender.id = :userId OR dd.borrow.borrower.id = :userId)")
    Page<DepositDispute> findByRelatedUser(@Param("userId") Long userId, Pageable pageable);
}
