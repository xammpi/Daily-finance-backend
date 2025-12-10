package com.expensetracker.dto.transaction;

import java.math.BigDecimal;

/**
 * Summary information for transaction search results
 * Includes aggregated totals for all matching transactions
 */
public record TransactionSearchSummary(
        BigDecimal totalAmount,
        Long transactionCount,
        BigDecimal totalExpenseAmount,
        BigDecimal totalIncomeAmount
) {
    public TransactionSearchSummary {
        totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        transactionCount = transactionCount != null ? transactionCount : 0L;
        totalExpenseAmount = totalExpenseAmount != null ? totalExpenseAmount : BigDecimal.ZERO;
        totalIncomeAmount = totalIncomeAmount != null ? totalIncomeAmount : BigDecimal.ZERO;
    }
}
