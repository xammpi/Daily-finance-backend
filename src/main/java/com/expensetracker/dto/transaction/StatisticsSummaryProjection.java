package com.expensetracker.dto.transaction;

import java.math.BigDecimal;

/**
 * Projection for batched statistics query results
 * Type-safe alternative to Object[] return type
 */
public record StatisticsSummaryProjection(
        BigDecimal todayAmount,
        BigDecimal weekAmount,
        BigDecimal monthAmount
) {
    public StatisticsSummaryProjection {
        // Ensure non-null values with defaults
        todayAmount = todayAmount != null ? todayAmount : BigDecimal.ZERO;
        weekAmount = weekAmount != null ? weekAmount : BigDecimal.ZERO;
        monthAmount = monthAmount != null ? monthAmount : BigDecimal.ZERO;
    }
}
