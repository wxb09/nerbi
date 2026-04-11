package com.neighbor.repository;

import com.neighbor.entity.Borrow;
import com.neighbor.enums.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    
    Page<Borrow> findByBorrowerId(Long borrowerId, Pageable pageable);
    
    Page<Borrow> findByLenderId(Long lenderId, Pageable pageable);
    
    Page<Borrow> findByBorrowerIdAndStatus(Long borrowerId, BorrowStatus status, Pageable pageable);
    
    Page<Borrow> findByLenderIdAndStatus(Long lenderId, BorrowStatus status, Pageable pageable);
    
    @Query("SELECT b FROM Borrow b WHERE b.lender.id = :lenderId AND b.status = :status")
    Page<Borrow> findByLenderIdAndStatusWithItem(@Param("lenderId") Long lenderId, 
                                                   @Param("status") BorrowStatus status, 
                                                   Pageable pageable);
    
    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.lender.id = :userId AND b.status = :status")
    Long countByLenderIdAndStatus(@Param("userId") Long userId, @Param("status") BorrowStatus status);
    
    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.borrower.id = :userId AND b.status = :status")
    Long countByBorrowerIdAndStatus(@Param("userId") Long userId, @Param("status") BorrowStatus status);
    
    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.lender.id = :userId AND b.status IN :statuses")
    Long countByLenderIdAndStatusIn(@Param("userId") Long userId, @Param("statuses") List<BorrowStatus> statuses);
    
    List<Borrow> findByItemIdAndStatus(Long itemId, BorrowStatus status);
    
    @Query("SELECT b FROM Borrow b JOIN FETCH b.item JOIN FETCH b.borrower WHERE b.status = :status AND b.endDate = :date")
    List<Borrow> findByStatusAndEndDateWithDetails(@Param("status") BorrowStatus status, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Borrow b JOIN FETCH b.item JOIN FETCH b.borrower WHERE b.status = :status AND b.endDate < :date")
    List<Borrow> findByStatusAndEndDateBeforeWithDetails(@Param("status") BorrowStatus status, @Param("date") LocalDate date);
    
    @Query("SELECT b FROM Borrow b JOIN FETCH b.item JOIN FETCH b.borrower WHERE b.status IN :statuses AND b.endDate < :date AND (b.lastRemindAt IS NULL OR b.lastRemindAt < :remindThreshold)")
    List<Borrow> findOverdueForReminder(@Param("statuses") List<BorrowStatus> statuses, @Param("date") LocalDate date, @Param("remindThreshold") LocalDateTime remindThreshold);

    Page<Borrow> findByStatus(BorrowStatus status, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.status = :status")
    Long countByStatus(@Param("status") BorrowStatus status);

    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.createdAt >= :since")
    Long countByCreatedAtAfter(@Param("since") java.time.LocalDateTime since);

    @Query("SELECT COUNT(b) FROM Borrow b WHERE b.status IN :statuses")
    Long countByStatusIn(@Param("statuses") List<BorrowStatus> statuses);
}
