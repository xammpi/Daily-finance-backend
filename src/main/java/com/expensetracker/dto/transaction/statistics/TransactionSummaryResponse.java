package com.expensetracker.dto.transaction.statistics;

import com.expensetracker.dto.currency.CurrencyResponse;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionSummaryResponse(
    String period,                    // "TODAY", "WEEK", "MONTH", etc.
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalExpenses,
    BigDecimal totalIncome,
    BigDecimal netAmount,             // income - expenses
    Long transactionCount,
    BigDecimal averageExpensePerDay,
    BigDecimal averageIncomePerDay,
    BigDecimal averageTransactionAmount,
    PeriodComparisonData comparison,  // null if compareWithPrevious=false
    CurrencyResponse currency
) {
}
