package com.expensetracker.dto.transaction.statistics;

import com.expensetracker.entity.CategoryType;

import java.math.BigDecimal;

public record CategoryBreakdownItem(
    Long categoryId,
    String categoryName,
    CategoryType categoryType,
    BigDecimal amount,
    Long transactionCount,
    BigDecimal percentage,             // (amount / totalAmount) * 100
    BigDecimal averageTransactionAmount // amount / transactionCount
) implements Comparable<CategoryBreakdownItem> {

    @Override
    public int compareTo(CategoryBreakdownItem other) {
        return other.amount.compareTo(this.amount); // Sort by amount DESC
    }
}
