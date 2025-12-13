package com.expensetracker.repository;

import com.expensetracker.dto.transaction.StatisticsSummaryProjection;
import com.expensetracker.dto.transaction.statistics.CategoryBreakdownProjection;
import com.expensetracker.dto.transaction.statistics.OverviewProjection;
import com.expensetracker.dto.transaction.statistics.RangeProjection;
import com.expensetracker.dto.transaction.statistics.TrendProjection;
import com.expensetracker.entity.CategoryType;
import com.expensetracker.entity.Transaction;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
    @Query("""
            SELECT t FROM Transaction t
                        JOIN FETCH t.category
                        JOIN FETCH t.user u
                        JOIN FETCH u.wallet
                        WHERE t.id = :id
            """)
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "false"))
    Optional<Transaction> findByIdWithRelations(@Param("id") Long id);

    /**
     * Optimized query for transaction deletion - fetches transaction with user and wallet
     */
    @Query("""
            SELECT t FROM Transaction t
                        JOIN FETCH t.category
                        JOIN FETCH t.user u
                        JOIN FETCH u.wallet
                        WHERE t.user.id = :userId AND t.id = :id
            """)
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "false"))
    Optional<Transaction> findByUserIdAndIdWithWallet(@Param("userId") Long userId, @Param("id") Long id);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
                        WHERE t.user.id = :userId
                        AND t.date BETWEEN :startDate AND :endDate
                        AND t.category.type = :type
            """)
    BigDecimal getSumByUserIdAndDateBetweenAndType(@Param("userId") Long userId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   @Param("type") CategoryType type);

    /**
     * Batch query to get today, week, and month statistics in a single database call
     * Uses type-safe DTO projection instead of Object[]
     * Efficient aggregation query that returns computed values without loading entities
     */
    @Query("""
            SELECT new StatisticsSummaryProjection(
                        COALESCE(SUM(CASE WHEN t.date = :today AND t.category.type = :type THEN t.amount ELSE 0 END), 0),
                        COALESCE(SUM(CASE WHEN t.date BETWEEN :weekStart AND :weekEnd AND t.category.type = :type THEN t.amount ELSE 0 END), 0),
                        COALESCE(SUM(CASE WHEN t.date BETWEEN :monthStart AND :monthEnd AND t.category.type = :type THEN t.amount ELSE 0 END), 0)
                        ) FROM Transaction t
                        WHERE t.user.id = :userId
            """)
    StatisticsSummaryProjection getStatisticsSummary(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd,
            @Param("type") CategoryType type
    );

    /**
     * Efficient batched query for overview statistics
     * Returns today, week, and month totals for both INCOME and EXPENSE in a single query
     */
    @Query("""
            SELECT new OverviewProjection(
                        COALESCE(SUM(CASE WHEN t.date = :today AND t.category.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0),
                        COALESCE(SUM(CASE WHEN t.date = :today AND t.category.type = 'INCOME' THEN t.amount ELSE 0 END), 0),
                        COALESCE(SUM(CASE WHEN t.date BETWEEN :weekStart AND :weekEnd AND t.category.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0),
                        COALESCE(SUM(CASE WHEN t.date BETWEEN :weekStart AND :weekEnd AND t.category.type = 'INCOME' THEN t.amount ELSE 0 END), 0),
                        COALESCE(SUM(CASE WHEN t.date BETWEEN :monthStart AND :monthEnd AND t.category.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0),
                        COALESCE(SUM(CASE WHEN t.date BETWEEN :monthStart AND :monthEnd AND t.category.type = 'INCOME' THEN t.amount ELSE 0 END), 0)
                        ) FROM Transaction t
                        WHERE t.user.id = :userId
            """)
    OverviewProjection getOverviewStatistics(
            @Param("userId") Long userId,
            @Param("today") LocalDate today,
            @Param("weekStart") LocalDate weekStart,
            @Param("weekEnd") LocalDate weekEnd,
            @Param("monthStart") LocalDate monthStart,
            @Param("monthEnd") LocalDate monthEnd
    );

    /**
     * Range statistics with optional type filter
     * Returns total expenses, income, and transaction count for a date range
     */
    @Query("""
            SELECT new RangeProjection(
                        COALESCE(SUM(CASE WHEN t.category.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0),
                        COALESCE(SUM(CASE WHEN t.category.type = 'INCOME' THEN t.amount ELSE 0 END), 0),
                       COUNT(t)
                        ) FROM Transaction t
                        WHERE t.user.id = :userId
                        AND t.date BETWEEN :startDate AND :endDate
                        AND (:type IS NULL OR t.category.type = :type)
            """)
    RangeProjection getRangeStatistics(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") CategoryType type
    );

    /**
     * Category breakdown with aggregation
     * Returns category-wise breakdown with sum and count
     */
    @Query("""
            SELECT t.category.id AS categoryId,
                        t.category.name AS categoryName,
                        CAST(t.category.type AS string) AS categoryType,
                        SUM(t.amount) AS amount,
                        COUNT(t) AS transactionCount
                        FROM Transaction t
                        WHERE t.user.id = :userId
                       AND t.date BETWEEN :startDate AND :endDate
                        AND (:type IS NULL OR t.category.type = :type)
                        GROUP BY t.category.id, t.category.name, t.category.type
                        ORDER BY SUM(t.amount) DESC
            """)
    List<CategoryBreakdownProjection> getCategoryBreakdown(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") CategoryType type
    );

    /**
     * Daily trends - group by date
     * Returns aggregated data for each day in the date range
     */
    @Query("""
            SELECT t.date AS date,
                        COALESCE(SUM(CASE WHEN t.category.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) AS expenses,
                       COALESCE(SUM(CASE WHEN t.category.type = 'INCOME' THEN t.amount ELSE 0 END), 0) AS income,
                        COUNT(t) AS transactionCount
                        FROM Transaction t
                        WHERE t.user.id = :userId
                        AND t.date BETWEEN :startDate AND :endDate
                        AND (:type IS NULL OR t.category.type = :type)
                        GROUP BY t.date
                        ORDER BY t.date
            """)
    List<TrendProjection> getDailyTrends(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") CategoryType type
    );

    /**
     * Weekly trends using native query with DATE_TRUNC
     * Returns aggregated data grouped by week (Monday-Sunday)
     */
    @Query(value = "SELECT " +
            "CAST(DATE_TRUNC('week', t.date) AS DATE) AS date, " +
            "COALESCE(SUM(CASE WHEN c.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) AS expenses, " +
            "COALESCE(SUM(CASE WHEN c.type = 'INCOME' THEN t.amount ELSE 0 END), 0) AS income, " +
            "COUNT(t.*) AS transaction_count " +
            "FROM transactions t " +
            "JOIN categories c ON t.category_id = c.id " +
            "WHERE t.user_id = :userId " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "AND (:type IS NULL OR c.type = CAST(:type AS VARCHAR)) " +
            "GROUP BY DATE_TRUNC('week', t.date) " +
            "ORDER BY DATE_TRUNC('week', t.date)",
            nativeQuery = true)
    List<TrendProjection> getWeeklyTrends(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") String type
    );

    /**
     * Monthly trends using native query with DATE_TRUNC
     * Returns aggregated data grouped by month
     */
    @Query(value = "SELECT " +
            "CAST(DATE_TRUNC('month', t.date) AS DATE) AS date, " +
            "COALESCE(SUM(CASE WHEN c.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) AS expenses, " +
            "COALESCE(SUM(CASE WHEN c.type = 'INCOME' THEN t.amount ELSE 0 END), 0) AS income, " +
            "COUNT(t.*) AS transaction_count " +
            "FROM transactions t " +
            "JOIN categories c ON t.category_id = c.id " +
            "WHERE t.user_id = :userId " +
            "AND t.date BETWEEN :startDate AND :endDate " +
            "AND (:type IS NULL OR c.type = CAST(:type AS VARCHAR)) " +
            "GROUP BY DATE_TRUNC('month', t.date) " +
            "ORDER BY DATE_TRUNC('month', t.date)",
            nativeQuery = true)
    List<TrendProjection> getMonthlyTrends(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") String type
    );
}
