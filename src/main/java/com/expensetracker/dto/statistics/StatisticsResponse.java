package com.expensetracker.dto.statistics;

import com.expensetracker.entity.Currency;

import java.math.BigDecimal;

public record StatisticsResponse(
        BigDecimal currentBalance,
        BigDecimal totalDeposits,
        BigDecimal totalExpenses,
        BigDecimal totalDepositsThisMonth,
        BigDecimal totalExpensesThisMonth,
        Currency currency
) {
}
