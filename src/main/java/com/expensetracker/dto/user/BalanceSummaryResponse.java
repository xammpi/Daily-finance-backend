package com.expensetracker.dto.user;

import com.expensetracker.entity.Currency;

import java.math.BigDecimal;

public record BalanceSummaryResponse(
        BigDecimal currentBalance,
        BigDecimal todayExpenses,
        BigDecimal weekExpenses,
        BigDecimal monthExpenses,
        Currency currency
) {
}
