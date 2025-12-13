package com.expensetracker.dto.transaction.statistics;

import com.expensetracker.dto.currency.CurrencyResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CategoryStatisticsResponse(
    String period,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalAmount,
    Long totalTransactionCount,
    List<CategoryBreakdownItem> categories,
    CurrencyResponse currency
) {
}
