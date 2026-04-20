package com.neighbor.repository;

import com.neighbor.entity.Post;
import com.neighbor.enums.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @EntityGraph(attributePaths = {"user", "community"})
    Page<Post> findByStatus(String status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "community"})
    Page<Post> findByCommunityIdAndStatus(Long communityId, String status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "community"})
    Page<Post> findByCommunityIdAndTypeAndStatus(Long communityId, PostType type, String status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "community"})
    Page<Post> findByTypeAndStatus(PostType type, String status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "community"})
    Page<Post> findByUserIdAndStatus(Long userId, String status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "community"})
    @Query("SELECT p FROM Post p WHERE p.community.id = :communityId AND p.status = :status ORDER BY p.likeCount DESC, p.createdAt DESC")
    Page<Post> findHotPosts(@Param("communityId") Long communityId, @Param("status") String status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "community"})
    @Query("SELECT p FROM Post p WHERE p.status = :status ORDER BY p.likeCount DESC, p.createdAt DESC")
    Page<Post> findHotPostsAll(@Param("status") String status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "community"})
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Post findByIdWithFetch(@Param("id") Long id);
}
