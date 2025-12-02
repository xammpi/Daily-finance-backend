package com.expensetracker.dto.expense;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @Digits(integer = 10, fraction = 2)
        BigDecimal amount,
        @NotNull(message = "Date is required")
        @PastOrPresent(message = "Date cannot be in the future")
        LocalDate date,
        @Size(max = 500, message = "Description is too long")
        String description,
        @NotNull(message = "Category is required")
        @Positive(message = "Invalid category")
        Integer categoryId
) {
}
