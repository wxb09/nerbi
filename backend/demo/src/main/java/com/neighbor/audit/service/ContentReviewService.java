package com.neighbor.audit.service;

import com.neighbor.audit.entity.AuditLog;
import com.neighbor.audit.repository.AuditLogRepository;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.entity.Post;
import com.neighbor.enums.ErrorCode;
import com.neighbor.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ContentReviewService {

    private static final Logger log = LoggerFactory.getLogger(ContentReviewService.class);

    private final AuditLogRepository auditLogRepository;
    private final PostRepository postRepository;

    public ContentReviewService(AuditLogRepository auditLogRepository, PostRepository postRepository) {
        this.auditLogRepository = auditLogRepository;
        this.postRepository = postRepository;
    }

    /**
     * 用户举报内容
     */
    public void reportContent(String targetType, Long targetId, Long reporterId, String reason) {
        log.info("[Report] 用户举报: reporterId={}, target={}#{}, reason={}", reporterId, targetType, targetId, reason);

        AuditLog report = new AuditLog();
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReviewType("REPORT");
        report.setAuditResult("PENDING");
        report.setReporterId(reporterId);
        report.setResult(reason);

        auditLogRepository.save(report);

        if ("POST".equals(targetType)) {
            Post post = postRepository.findById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));
            post.setStatus("UNDER_REVIEW");
            postRepository.save(post);
        }

        log.info("[Report] 举报已提交: reportId={}", report.getId());
    }

    /**
     * 管理员获取举报列表
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getPendingReports(Pageable pageable) {
        return auditLogRepository.findByReviewTypeAndAuditResult("REPORT", "PENDING", pageable);
    }

    /**
     * 管理员获取所有举报记录
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAllReports(Pageable pageable) {
        return auditLogRepository.findByReviewType("REPORT", pageable);
    }

    /**
     * 管理员处理举报
     */
    public void resolveReport(Long reportId, Long handlerId, String action, String result) {
        log.info("[Report] 处理举报: reportId={}, handlerId={}, action={}", reportId, handlerId, action);

        AuditLog report = auditLogRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        if (!"PENDING".equals(report.getAuditResult())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该举报已处理");
        }

        report.setHandlerId(handlerId);
        report.setResult(result);

        if ("DELETE".equals(action)) {
            report.setAuditResult("RESOLVED");
            if ("POST".equals(report.getTargetType())) {
                Post post = postRepository.findById(report.getTargetId()).orElse(null);
                if (post != null) {
                    post.setStatus("DELETED");
                    postRepository.save(post);
                }
            }
        } else if ("REJECT".equals(action)) {
            report.setAuditResult("REJECTED");
            if ("POST".equals(report.getTargetType())) {
                Post post = postRepository.findById(report.getTargetId()).orElse(null);
                if (post != null) {
                    post.setStatus("PUBLISHED");
                    postRepository.save(post);
                }
            }
        } else {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "无效的处理动作");
        }

        auditLogRepository.save(report);
        log.info("[Report] 举报处理完成: reportId={}, action={}", reportId, action);
    }
}
