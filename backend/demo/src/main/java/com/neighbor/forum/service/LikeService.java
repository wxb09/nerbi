package com.neighbor.forum.service;

import com.neighbor.entity.Comment;
import com.neighbor.entity.Like;
import com.neighbor.entity.Post;
import com.neighbor.entity.User;
import com.neighbor.forum.dto.LikeRequest;
import com.neighbor.forum.dto.LikeStatusDTO;
import com.neighbor.repository.CommentRepository;
import com.neighbor.repository.LikeRepository;
import com.neighbor.repository.PostRepository;
import com.neighbor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LikeService {

    private static final Logger log = LoggerFactory.getLogger(LikeService.class);

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public LikeService(LikeRepository likeRepository, UserRepository userRepository,
                       PostRepository postRepository, CommentRepository commentRepository) {
        this.likeRepository = likeRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public LikeStatusDTO toggleLike(LikeRequest request, Long userId) {
        boolean exists = likeRepository.existsByUserIdAndTargetTypeAndTargetId(
                userId, request.targetType(), request.targetId());

        if (exists) {
            likeRepository.deleteByUserIdAndTargetTypeAndTargetId(
                    userId, request.targetType(), request.targetId());
            log.info("[LikeService] 取消点赞: userId={}, targetType={}, targetId={}",
                    userId, request.targetType(), request.targetId());

            updateTargetLikeCount(request.targetType(), request.targetId(), -1);
            return new LikeStatusDTO(false, getTargetLikeCount(request.targetType(), request.targetId()));
        } else {
            User user = userRepository.getReferenceById(userId);
            Like like = new Like();
            like.setUser(user);
            like.setTargetType(request.targetType());
            like.setTargetId(request.targetId());
            likeRepository.save(like);
            log.info("[LikeService] 点赞: userId={}, targetType={}, targetId={}",
                    userId, request.targetType(), request.targetId());

            updateTargetLikeCount(request.targetType(), request.targetId(), 1);
            return new LikeStatusDTO(true, getTargetLikeCount(request.targetType(), request.targetId()));
        }
    }

    @Transactional(readOnly = true)
    public LikeStatusDTO getLikeStatus(String targetType, Long targetId, Long userId) {
        boolean liked = false;
        if (userId != null) {
            liked = likeRepository.existsByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId);
        }
        int count = getTargetLikeCount(targetType, targetId);
        return new LikeStatusDTO(liked, count);
    }

    private void updateTargetLikeCount(String targetType, Long targetId, int delta) {
        if ("POST".equals(targetType)) {
            postRepository.findById(targetId).ifPresent(post -> {
                post.setLikeCount(Math.max(0, post.getLikeCount() + delta));
                postRepository.save(post);
            });
        } else if ("COMMENT".equals(targetType)) {
            commentRepository.findById(targetId).ifPresent(comment -> {
                comment.setLikeCount(Math.max(0, comment.getLikeCount() + delta));
                commentRepository.save(comment);
            });
        }
    }

    private int getTargetLikeCount(String targetType, Long targetId) {
        Long count = likeRepository.countByTargetTypeAndTargetId(targetType, targetId);
        return count != null ? count.intValue() : 0;
    }
}
