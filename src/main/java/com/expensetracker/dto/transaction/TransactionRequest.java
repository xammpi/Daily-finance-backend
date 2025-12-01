package com.expensetracker.dto.transaction;

import com.expensetracker.entity.Transaction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Type is required")
    private Transaction.TransactionType type;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String description;

    private String notes;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
