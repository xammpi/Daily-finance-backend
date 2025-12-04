package com.expensetracker.dto.expense;

import java.math.BigDecimal;

public record CategoryStatisticsItem(
        Long categoryId,
        String categoryName,
        BigDecimal totalAmount,
        Long expenseCount,
        BigDecimal percentage
) {
}
