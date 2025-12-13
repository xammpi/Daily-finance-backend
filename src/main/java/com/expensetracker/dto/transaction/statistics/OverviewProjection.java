package com.expensetracker.dto.transaction.statistics;

import java.math.BigDecimal;

public record OverviewProjection(
    BigDecimal todayExpenses,
    BigDecimal todayIncome,
    BigDecimal weekExpenses,
    BigDecimal weekIncome,
    BigDecimal monthExpenses,
    BigDecimal monthIncome
) {
}
