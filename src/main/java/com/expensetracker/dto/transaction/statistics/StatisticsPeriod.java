package com.expensetracker.dto.transaction.statistics;

public enum StatisticsPeriod {
    TODAY,
    WEEK,        // Current week (Monday-Sunday)
    MONTH,       // Current month
    YEAR,        // Current year
    CUSTOM,      // Requires startDate and endDate
    ALL_TIME     // All transactions
}
