package com.expensetracker.repository;

import com.expensetracker.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByUserIdAndId(Long userId, Long id);

    List<Transaction> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Transaction e " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal getSumByUserIdAndDateBetween(@Param("userId") Long userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Transaction e WHERE e.user.id = :userId")
    BigDecimal getTotalTransactionsByUserId(@Param("userId") Long userId);

}
