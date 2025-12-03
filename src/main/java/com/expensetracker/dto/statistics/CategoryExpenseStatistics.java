package com.expensetracker.dto.statistics;

import java.math.BigDecimal;

public record CategoryExpenseStatistics(
        Long categoryId,
        String categoryName,
        BigDecimal totalAmount,
        Long expenseCount
) {
}
