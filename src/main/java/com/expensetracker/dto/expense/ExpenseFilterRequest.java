package com.expensetracker.dto.expense;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseFilterRequest(
        Long categoryId,
        LocalDate startDate,
        LocalDate endDate,
        @PositiveOrZero(message = "Minimum amount must be zero or positive")
        BigDecimal minAmount,
        @PositiveOrZero(message = "Maximum amount must be zero or positive")
        BigDecimal maxAmount,
        @Min(value = 0, message = "Page number must be zero or positive")
        Integer page,
        @Min(value = 1, message = "Page size must be at least 1")
        Integer size
) {
    public ExpenseFilterRequest {
        // Default values
        if (page == null) page = 0;
        if (size == null) size = 10;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}
