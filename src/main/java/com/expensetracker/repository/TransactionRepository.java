package com.expensetracker.repository;

import com.expensetracker.dto.transaction.StatisticsSummaryProjection;
import com.expensetracker.entity.CategoryType;
import com.expensetracker.entity.Transaction;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    /**
     * Override findAll with EntityGraph to prevent N+1 queries in search results
     * Eagerly fetches category and user relationships in a single query
     */
    @EntityGraph(attributePaths = {"category", "user"})
    @Override
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);

    @Query("SELECT t FROM Transaction t JOIN FETCH t.category WHERE t.user.id = :userId AND t.id = :id")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "false"))
    Optional<Transaction> findByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);

    /**
     * Optimized query for transaction updates - fetches transaction with all required relationships
     * Includes category, user, and wallet to avoid N+1 queries during update operations
     */
    @Query("SELECT t FROM Transaction t " +
            "JOIN FETCH t.category " +
            "JOIN FETCH t.user u " +
            "JOIN FETCH u.wallet " +
            "WHERE t.id = :id")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "false"))
    Optional<Transaction> findByIdWithRelations(@Param("id") Long id);

    /**
     * Optimized query for transaction deletion - fetches transaction with user and wallet
     */
    @Query("SELECT t FROM Transaction t " +
            "JOIN FETCH t.category " +
            "JOIN FETCH t.user u " +
            "JOIN FETCH u.wallet " +
            "WHERE t.user.id = :userId AND t.id = :id")
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "false"))
    Optional<Transaction> findByUserIdAndIdWithWallet(@Param("userId") Long userId, @Param("id") Long id);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "AND t.category.type = :type")
    BigDecimal getSumByUserIdAndDateBetweenAndType(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   @Param("type") CategoryType type);

    /**
     * Batch query to get today, week, and month statistics in a single database call
     * Uses type-safe DTO projection instead of Object[]
     * Efficient aggregation query that returns computed values without loading entities
     */
    @Query("SELECT new com.expensetracker.dto.transaction.StatisticsSummaryProjection(" +
            "COALESCE(SUM(CASE WHEN t.date = :today AND t.category.type = :type THEN t.amount ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN t.date BETWEEN :weekStart AND :weekEnd AND t.category.type = :type THEN t.amount ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN t.date BETWEEN :monthStart AND :monthEnd AND t.category.type = :type THEN t.amount ELSE 0 END), 0)" +
            ") FROM Transaction t " +
            "WHERE t.user.id = :userId")
    StatisticsSummaryProjection getStatisticsSummary(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd,
            @Param("type") CategoryType type
    );
}
