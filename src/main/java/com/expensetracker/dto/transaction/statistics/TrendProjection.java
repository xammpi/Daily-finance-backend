package com.expensetracker.dto.transaction.statistics;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TrendProjection {
    LocalDate getDate();
    BigDecimal getExpenses();
    BigDecimal getIncome();
    Long getTransactionCount();
}
