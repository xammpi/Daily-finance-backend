package com.expensetracker.dto.transaction;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        @Digits(integer = 12, fraction = 2, message = "Invalid format (max 10 digits, 2 decimals)")
        BigDecimal amount,

        @NotNull(message = "Date is required")
        @PastOrPresent(message = "Date cannot be in the future")
        LocalDate date,

        @Size(max = 500, message = "Description cannot exceed 255 characters")
        String description,

        @NotNull(message = "Category is required")
        @Positive(message = "Invalid category")
        Long categoryId
) {
}
