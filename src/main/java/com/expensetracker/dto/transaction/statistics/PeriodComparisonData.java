package com.expensetracker.dto.transaction.statistics;

import java.math.BigDecimal;

public record PeriodComparisonData(
    BigDecimal previousExpenses,
    BigDecimal previousIncome,
    BigDecimal expensesChange,         // current - previous
    BigDecimal expensesChangePercent,  // ((current - previous) / previous) * 100
    BigDecimal incomeChange,
    BigDecimal incomeChangePercent
) {
}
