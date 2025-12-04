package com.expensetracker.dto.expense;

import com.expensetracker.entity.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CategoryStatisticsResponse(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalExpenses,
        List<CategoryStatisticsItem> categoryBreakdown,
        Currency currency
) {
}
