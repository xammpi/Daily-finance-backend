package com.expensetracker.dto.expense;

import com.expensetracker.entity.Currency;

import java.math.BigDecimal;

public record ExpenseStatisticsResponse(
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
