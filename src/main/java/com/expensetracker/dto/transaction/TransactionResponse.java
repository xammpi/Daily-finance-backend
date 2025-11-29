package com.expensetracker.dto.transaction;

import com.expensetracker.entity.Transaction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private Transaction.TransactionType type;
    private LocalDate date;
    private String description;
    private String notes;
    private Long accountId;
    private String accountName;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
