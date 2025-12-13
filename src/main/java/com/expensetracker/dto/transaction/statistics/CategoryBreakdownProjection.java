package com.expensetracker.dto.transaction.statistics;

import java.math.BigDecimal;

public interface CategoryBreakdownProjection {
    Long getCategoryId();
    String getCategoryName();
    String getCategoryType();  // CategoryType stored as String in DB
    BigDecimal getAmount();
    Long getTransactionCount();
}
