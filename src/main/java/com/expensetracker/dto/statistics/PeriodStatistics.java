package com.expensetracker.dto.statistics;

import com.expensetracker.entity.Currency;

import java.math.BigDecimal;

public record PeriodStatistics(
        String period,                      // "daily", "monthly", "yearly"
        BigDecimal currentBalance,          // Current wallet balance
        BigDecimal totalDeposits,           // Total deposits in period
        BigDecimal totalExpenses,           // Total expenses in period
        BigDecimal netChange,               // Deposits - Expenses
        Integer depositCount,               // Number of deposits
        Integer expenseCount,               // Number of expenses
        Currency currency
) {
}
