package com.neighbor.repository;

import com.neighbor.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByPostIdAndStatus(Long postId, String status, Pageable pageable);

    Page<Comment> findByPostIdAndParentIdIsNullAndStatus(Long postId, String status, Pageable pageable);

    Page<Comment> findByParentIdAndStatus(Long parentId, String status, Pageable pageable);

    List<Comment> findByParentId(Long parentId);

    Long countByPostIdAndStatus(Long postId, String status);
}
