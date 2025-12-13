package com.expensetracker.dto.transaction.statistics;

import java.math.BigDecimal;

public record RangeProjection(
    BigDecimal totalExpenses,
    BigDecimal totalIncome,
    Long transactionCount
) {
}
