package com.expensetracker.dto.transaction.statistics;

import com.expensetracker.dto.currency.CurrencyResponse;

import java.math.BigDecimal;

public record TransactionOverviewResponse(
    BigDecimal todayExpenses,
    BigDecimal todayIncome,
    BigDecimal weekExpenses,
    BigDecimal weekIncome,
    BigDecimal monthExpenses,
    BigDecimal monthIncome,
    CurrencyResponse currency
) {
}
