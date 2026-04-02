package com.neighbor.repository;

import com.neighbor.entity.Item;
import com.neighbor.enums.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    
    Page<Item> findByStatusIn(List<ItemStatus> statuses, Pageable pageable);
    
    Page<Item> findByStatusInAndCommunityId(List<ItemStatus> statuses, Long communityId, Pageable pageable);
    
    Page<Item> findByStatusInAndCategoryId(List<ItemStatus> statuses, Long categoryId, Pageable pageable);
    
    Page<Item> findByStatusInAndCommunityIdAndCategoryId(List<ItemStatus> statuses, Long communityId, Long categoryId, Pageable pageable);
    
    Page<Item> findByOwnerId(Long ownerId, Pageable pageable);
    
    Page<Item> findByOwnerIdAndStatus(Long ownerId, ItemStatus status, Pageable pageable);
    
    @Query("SELECT i FROM Item i WHERE i.status IN :statuses AND " +
           "(LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Item> searchByKeyword(@Param("keyword") String keyword, 
                               @Param("statuses") List<ItemStatus> statuses, 
                               Pageable pageable);
    
    @Query("SELECT i FROM Item i WHERE i.status = :status AND i.category.id = :categoryId " +
           "AND i.id != :excludeId ORDER BY i.createdAt DESC LIMIT :limit")
    List<Item> findSimilarItems(@Param("categoryId") Long categoryId, 
                                @Param("status") ItemStatus status,
                                @Param("excludeId") Long excludeId,
                                @Param("limit") int limit);
    
    @Query("SELECT COUNT(i) FROM Item i WHERE i.owner.id = :ownerId AND i.status = :status")
    Long countByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") ItemStatus status);
}
