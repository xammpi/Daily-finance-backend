package com.expensetracker.dto.transaction.statistics;

import com.expensetracker.dto.currency.CurrencyResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RangeStatisticsResponse(
    LocalDate startDate,
    LocalDate endDate,
    Integer daysCount,
    BigDecimal totalExpenses,
    BigDecimal totalIncome,
    BigDecimal netAmount,
    Long transactionCount,
    BigDecimal averagePerDay,          // total / daysCount
    List<CategoryBreakdownItem> topCategories,  // top 5
    PeriodComparisonData comparison,   // null if compareWithPrevious=false
    CurrencyResponse currency
) {
}
