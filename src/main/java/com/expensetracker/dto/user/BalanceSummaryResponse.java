package com.expensetracker.dto.user;

import com.expensetracker.entity.Currency;

import java.math.BigDecimal;

public record BalanceSummaryResponse(
        BigDecimal currentBalance,
        BigDecimal totalExpensesThisMonth,
        BigDecimal remainingBalance,
        Currency currency
) {
}
