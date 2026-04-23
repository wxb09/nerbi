package com.neighbor.forum.service;

import com.neighbor.audit.dto.AuditResult;
import com.neighbor.audit.service.SensitiveWordService;
import com.neighbor.common.exception.BusinessException;
import com.neighbor.entity.Community;
import com.neighbor.entity.Post;
import com.neighbor.entity.User;
import com.neighbor.enums.ErrorCode;
import com.neighbor.enums.PostType;
import com.neighbor.forum.dto.CreatePostRequest;
import com.neighbor.forum.dto.PostDetailDTO;
import com.neighbor.forum.dto.PostListDTO;
import com.neighbor.repository.LikeRepository;
import com.neighbor.repository.PostRepository;
import com.neighbor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final SensitiveWordService sensitiveWordService;

    public PostService(PostRepository postRepository, UserRepository userRepository, 
                       LikeRepository likeRepository, SensitiveWordService sensitiveWordService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.sensitiveWordService = sensitiveWordService;
    }

    @Transactional(readOnly = true)
    public Page<PostListDTO> getPosts(Long communityId, String type, String sort, Long currentUserId, Pageable pageable) {
        PostType postType = null;
        if (type != null) {
            try {
                postType = PostType.valueOf(type);
            } catch (IllegalArgumentException e) {
                postType = null;
            }
        }

        if ("hot".equals(sort)) {
            return getHotPosts(communityId, postType, currentUserId, pageable);
        }

        Page<Post> posts;
        if (communityId != null && postType != null) {
            posts = postRepository.findByCommunityIdAndTypeAndStatus(communityId, postType, "PUBLISHED", pageable);
        } else if (communityId != null) {
            posts = postRepository.findByCommunityIdAndStatus(communityId, "PUBLISHED", pageable);
        } else if (postType != null) {
            posts = postRepository.findByTypeAndStatus(postType, "PUBLISHED", pageable);
        } else {
            posts = postRepository.findByStatus("PUBLISHED", pageable);
        }

        return posts.map(post -> toListDTO(post, currentUserId));
    }

    @Transactional(readOnly = true)
    public Page<PostListDTO> getHotPosts(Long communityId, PostType postType, Long currentUserId, Pageable pageable) {
        Page<Post> posts;
        if (communityId != null && postType != null) {
            posts = postRepository.findHotPostsByCommunityAndType(communityId, postType, "PUBLISHED", pageable);
        } else if (communityId != null) {
            posts = postRepository.findHotPosts(communityId, "PUBLISHED", pageable);
        } else if (postType != null) {
            posts = postRepository.findHotPostsByType(postType, "PUBLISHED", pageable);
        } else {
            posts = postRepository.findHotPostsAll("PUBLISHED", pageable);
        }
        return posts.map(post -> toListDTO(post, currentUserId));
    }

    @Transactional(readOnly = true)
    public PostDetailDTO getPostById(Long id, Long currentUserId) {
        Post post = postRepository.findByIdWithFetch(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.ITEM_NOT_FOUND);
        }

        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        return toDetailDTO(post, currentUserId);
    }

    public Long createPost(CreatePostRequest request, Long userId) {
        log.info("[PostService] 创建帖子: userId={}, type={}", userId, request.type());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Post post = new Post();
        post.setUser(user);
        post.setTitle(request.title());
        post.setContent(request.content());
        post.setStatus("PUBLISHED");

        if (request.type() != null) {
            try {
                post.setType(PostType.valueOf(request.type()));
            } catch (IllegalArgumentException e) {
                post.setType(PostType.NORMAL);
            }
        }

        if (request.images() != null && !request.images().isEmpty()) {
            post.setImages(String.join(",", request.images()));
        }

        if (request.tags() != null && !request.tags().isEmpty()) {
            post.setTags(String.join(",", request.tags()));
        }

        if (request.communityId() != null) {
            Community community = new Community();
            community.setId(request.communityId());
            post.setCommunity(community);
        } else if (user.getCommunity() != null) {
            post.setCommunity(user.getCommunity());
        }

        String auditContent = (request.title() != null ? request.title() + " " : "") + request.content();
        AuditResult auditResult = sensitiveWordService.auditText(auditContent, "POST", 0L);

        if (!auditResult.passed()) {
            log.warn("[PostService] 帖子被拦截: userId={}, reason={}", userId, auditResult.reason());
            throw new BusinessException(ErrorCode.SENSITIVE_CONTENT, "内容包含敏感词，请修改后重试");
        }

        Post saved = postRepository.save(post);
        log.info("[PostService] 帖子已创建: postId={}", saved.getId());
        return saved.getId();
    }

    public void updatePost(Long postId, CreatePostRequest request, Long userId) {
        log.info("[PostService] 更新帖子: postId={}, userId={}", postId, userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (request.title() != null) {
            post.setTitle(request.title());
        }
        if (request.content() != null) {
            post.setContent(request.content());
        }
        if (request.type() != null) {
            try {
                post.setType(PostType.valueOf(request.type()));
            } catch (IllegalArgumentException e) {
                post.setType(PostType.NORMAL);
            }
        }
        if (request.images() != null) {
            post.setImages(request.images().isEmpty() ? null : String.join(",", request.images()));
        }
        if (request.tags() != null) {
            post.setTags(request.tags().isEmpty() ? null : String.join(",", request.tags()));
        }

        postRepository.save(post);
        log.info("[PostService] 帖子已更新: postId={}", postId);
    }

    public void deletePost(Long postId, Long userId) {
        log.info("[PostService] 删除帖子: postId={}, userId={}", postId, userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ITEM_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        post.setStatus("DELETED");
        postRepository.save(post);
        log.info("[PostService] 帖子已删除: postId={}", postId);
    }

    @Transactional(readOnly = true)
    public Page<PostListDTO> getMyPosts(Long userId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserIdAndStatus(userId, "PUBLISHED", pageable);
        return posts.map(post -> toListDTO(post, userId));
    }

    private PostListDTO toListDTO(Post post, Long currentUserId) {
        User author = post.getUser();
        PostListDTO.UserInfo userInfo = new PostListDTO.UserInfo(
                author.getId(),
                author.getNickname(),
                author.getAvatar(),
                author.getBuilding()
        );

        boolean likedByMe = false;
        if (currentUserId != null) {
            likedByMe = likeRepository.existsByUserIdAndTargetTypeAndTargetId(
                    currentUserId, "POST", post.getId());
        }

        String communityName = null;
        if (post.getCommunity() != null) {
            communityName = post.getCommunity().getName();
        }

        return new PostListDTO(
                post.getId(),
                post.getType().name(),
                post.getTitle(),
                post.getContent(),
                post.getImages(),
                post.getTags(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getViewCount(),
                post.getStatus(),
                likedByMe,
                userInfo,
                post.getCommunity() != null ? post.getCommunity().getId() : null,
                communityName,
                post.getCreatedAt() != null ? post.getCreatedAt().toString() : null
        );
    }

    private PostDetailDTO toDetailDTO(Post post, Long currentUserId) {
        User author = post.getUser();
        PostDetailDTO.UserInfo userInfo = new PostDetailDTO.UserInfo(
                author.getId(),
                author.getNickname(),
                author.getAvatar(),
                author.getBuilding(),
                author.getBio()
        );

        boolean likedByMe = false;
        if (currentUserId != null) {
            likedByMe = likeRepository.existsByUserIdAndTargetTypeAndTargetId(
                    currentUserId, "POST", post.getId());
        }

        List<String> images = post.getImages() != null
                ? Arrays.stream(post.getImages().split(",")).filter(s -> !s.isBlank()).collect(Collectors.toList())
                : List.of();

        List<String> tags = post.getTags() != null
                ? Arrays.stream(post.getTags().split(",")).filter(s -> !s.isBlank()).collect(Collectors.toList())
                : List.of();

        String communityName = null;
        if (post.getCommunity() != null) {
            communityName = post.getCommunity().getName();
        }

        return new PostDetailDTO(
                post.getId(),
                post.getType().name(),
                post.getTitle(),
                post.getContent(),
                images,
                tags,
                post.getLikeCount(),
                post.getCommentCount(),
                post.getViewCount(),
                post.getStatus(),
                likedByMe,
                userInfo,
                post.getCommunity() != null ? post.getCommunity().getId() : null,
                communityName,
                post.getCreatedAt() != null ? post.getCreatedAt().toString() : null,
                post.getUpdatedAt() != null ? post.getUpdatedAt().toString() : null
        );
    }
}
