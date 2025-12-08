package com.expensetracker.dto.transaction;

import com.expensetracker.entity.Currency;

import java.math.BigDecimal;

public record TransactionStatisticsResponse(
        BigDecimal todayExpenses,
        BigDecimal weekExpenses,
        BigDecimal monthExpenses,
        BigDecimal totalExpenses,
        BigDecimal averageDailyExpenses,
        BigDecimal averageWeeklyExpenses,
        BigDecimal averageMonthlyExpenses,
        BigDecimal previousWeekExpenses,
        BigDecimal previousMonthExpenses,
        Currency currency
) {
}
