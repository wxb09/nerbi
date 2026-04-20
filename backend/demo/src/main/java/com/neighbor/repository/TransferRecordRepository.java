package com.neighbor.repository;

import com.neighbor.entity.TransferRecord;
import com.neighbor.enums.TransferStatus;
import com.neighbor.enums.TransferType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRecordRepository extends JpaRepository<TransferRecord, Long> {

    List<TransferRecord> findByBorrowId(Long borrowId);

    List<TransferRecord> findByToUserId(Long toUserId);

    List<TransferRecord> findByBorrowIdAndTransferType(Long borrowId, TransferType transferType);

    boolean existsByBorrowIdAndTransferTypeAndStatus(Long borrowId, TransferType transferType, TransferStatus status);
}
