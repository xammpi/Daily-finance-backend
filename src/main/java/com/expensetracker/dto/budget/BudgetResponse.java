package com.expensetracker.dto.budget;

import com.expensetracker.entity.Budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BudgetResponse(
        Long id,
        String name,
        BigDecimal amount,
        Budget.BudgetPeriod period,
        LocalDate startDate,
        LocalDate endDate,
        Boolean active,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

}
