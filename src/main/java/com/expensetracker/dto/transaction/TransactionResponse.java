package com.expensetracker.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        BigDecimal amount,
        LocalDate date,
        String description,
        Long categoryId,
        String categoryName
) {
}
