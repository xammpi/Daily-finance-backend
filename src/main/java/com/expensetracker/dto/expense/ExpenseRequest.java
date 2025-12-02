package com.expensetracker.dto.expense;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
        @NotNull
        @Positive
        BigDecimal amount,
        @FutureOrPresent
        LocalDate date,
        String description,
        @NotNull
        Integer categoryId
) {
}
