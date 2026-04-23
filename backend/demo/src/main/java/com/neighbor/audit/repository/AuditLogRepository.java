package com.neighbor.audit.repository;

import com.neighbor.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByTargetTypeAndTargetId(String targetType, Long targetId, Pageable pageable);

    Page<AuditLog> findByTargetType(String targetType, Pageable pageable);

    Page<AuditLog> findByAuditResult(String auditResult, Pageable pageable);

    Page<AuditLog> findByReviewTypeAndAuditResult(String reviewType, String auditResult, Pageable pageable);

    Page<AuditLog> findByReviewType(String reviewType, Pageable pageable);
}
