package com.expensetracker.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        String description,
        Integer categoryId
) {
}
