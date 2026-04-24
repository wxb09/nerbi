package com.neighbor.forum.service;

import com.neighbor.audit.dto.AuditResult;
import com.neighbor.audit.service.SensitiveWordService;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.entity.Comment;
import com.neighbor.entity.Post;
import com.neighbor.entity.User;
import com.neighbor.enums.ErrorCode;
import com.neighbor.forum.dto.CommentDTO;
import com.neighbor.forum.dto.CreateCommentRequest;
import com.neighbor.repository.CommentRepository;
import com.neighbor.repository.LikeRepository;
import com.neighbor.repository.PostRepository;
import com.neighbor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final SensitiveWordService sensitiveWordService;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository,
                          UserRepository userRepository, LikeRepository likeRepository,
                          SensitiveWordService sensitiveWordService) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.sensitiveWordService = sensitiveWordService;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "comments", key = "#postId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<CommentDTO> getCommentsByPostId(Long postId, Long currentUserId, Pageable pageable) {
        postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        Page<Comment> comments = commentRepository.findByPostIdAndParentIdIsNullAndStatus(
                postId, "PUBLISHED", pageable);

        return comments.map(comment -> toDTO(comment, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> getReplies(Long parentCommentId, Long currentUserId, Pageable pageable) {
        Page<Comment> replies = commentRepository.findByParentIdAndStatus(
                parentCommentId, "PUBLISHED", pageable);
        return replies.map(reply -> toDTO(reply, currentUserId));
    }

    @Transactional(readOnly = true)
    public CommentDTO getCommentById(Long commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR));
        return toDTO(comment, currentUserId);
    }

    @CacheEvict(value = {"comments", "postDetail"}, allEntries = true)
    public Long createComment(CreateCommentRequest request, Long userId) {
        log.info("[CommentService] 创建评论: userId={}, postId={}", userId, request.postId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        AuditResult auditResult = sensitiveWordService.auditText(request.content(), "COMMENT", 0L);
        if (!auditResult.passed()) {
            log.warn("[CommentService] 评论被拦截: userId={}, reason={}", userId, auditResult.reason());
            throw new BusinessException(ErrorCode.SENSITIVE_CONTENT, "内容包含敏感词，请修改后重试");
        }

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(request.content());

        if (request.parentId() != null) {
            Comment parent = commentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR));
            comment.setParent(parent);
        }

        Comment saved = commentRepository.save(comment);

        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        log.info("[CommentService] 评论已创建: commentId={}", saved.getId());
        return saved.getId();
    }

    @CacheEvict(value = {"comments", "postDetail"}, allEntries = true)
    public void deleteComment(Long commentId, Long userId) {
        log.info("[CommentService] 删除评论: commentId={}, userId={}", commentId, userId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAM_ERROR));

        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        comment.setStatus("DELETED");
        commentRepository.save(comment);

        Post post = comment.getPost();
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postRepository.save(post);

        log.info("[CommentService] 评论已删除: commentId={}", commentId);
    }

    private CommentDTO toDTO(Comment comment, Long currentUserId) {
        User author = comment.getUser();
        CommentDTO.UserInfo userInfo = new CommentDTO.UserInfo(
                author.getId(),
                author.getNickname(),
                author.getAvatar(),
                author.getBuilding()
        );

        boolean likedByMe = false;
        if (currentUserId != null) {
            likedByMe = likeRepository.existsByUserIdAndTargetTypeAndTargetId(
                    currentUserId, "COMMENT", comment.getId());
        }

        return new CommentDTO(
                comment.getId(),
                comment.getContent(),
                comment.getLikeCount(),
                likedByMe,
                userInfo,
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getCreatedAt() != null ? comment.getCreatedAt().toString() : null
        );
    }
}
