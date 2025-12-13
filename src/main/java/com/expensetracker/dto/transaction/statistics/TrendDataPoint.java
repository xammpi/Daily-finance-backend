package com.expensetracker.dto.transaction.statistics;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrendDataPoint(
    LocalDate date,                    // Start date of the period
    BigDecimal expenses,
    BigDecimal income,
    BigDecimal netAmount,
    Long transactionCount
) {
}
