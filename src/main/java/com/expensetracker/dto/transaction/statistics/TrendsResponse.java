package com.expensetracker.dto.transaction.statistics;

import com.expensetracker.dto.currency.CurrencyResponse;

import java.time.LocalDate;
import java.util.List;

public record TrendsResponse(
    LocalDate startDate,
    LocalDate endDate,
    String groupBy,                    // "DAY", "WEEK", "MONTH"
    List<TrendDataPoint> dataPoints,
    CurrencyResponse currency
) {
}
