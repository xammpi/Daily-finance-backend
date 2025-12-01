package com.expensetracker.repository;

import com.expensetracker.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.date DESC")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.date BETWEEN :startDate AND :endDate")
    List<Transaction> findByUserIdAndDateBetween(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Transaction t WHERE t.category.id = :categoryId ORDER BY t.date DESC")
    Page<Transaction> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.type = :type")
    List<Transaction> findByUserIdAndType(@Param("userId") Long userId,
                                          @Param("type") Transaction.TransactionType type);
}
