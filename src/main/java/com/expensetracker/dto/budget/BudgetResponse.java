package com.expensetracker.dto.budget;

import com.expensetracker.entity.Budget;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BudgetResponse {
    private Long id;
    private String name;
    private BigDecimal amount;
    private Budget.BudgetPeriod period;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
