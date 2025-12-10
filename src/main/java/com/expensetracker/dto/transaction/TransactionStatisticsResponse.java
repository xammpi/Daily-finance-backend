package com.expensetracker.dto.transaction;

import com.expensetracker.dto.currency.CurrencyResponse;
import com.expensetracker.entity.Currency;

import java.math.BigDecimal;

public record TransactionStatisticsResponse(
        BigDecimal todayExpenses,
        BigDecimal weekExpenses,
        BigDecimal monthExpenses,
        CurrencyResponse currency
) {
}
