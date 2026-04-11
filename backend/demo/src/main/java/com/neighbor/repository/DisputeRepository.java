package com.neighbor.repository;

import com.neighbor.entity.Dispute;
import com.neighbor.enums.DisputeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    Page<Dispute> findByStatus(DisputeStatus status, Pageable pageable);

    Page<Dispute> findByBorrowId(Long borrowId, Pageable pageable);

    Page<Dispute> findByReporterId(Long reporterId, Pageable pageable);

    boolean existsByBorrowIdAndStatusIn(Long borrowId, java.util.List<DisputeStatus> statuses);

    Long countByStatus(DisputeStatus status);

    @Query("SELECT d FROM Dispute d WHERE " +
           "(:status IS NULL OR d.status = :status)")
    Page<Dispute> searchDisputes(@Param("status") DisputeStatus status, Pageable pageable);
}
