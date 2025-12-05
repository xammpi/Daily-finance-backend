package com.expensetracker.dto.common;

/**
 * Search/filter operations for dynamic queries
 */
public enum SearchOperation {
    /**
     * Equality: field = value
     */
    EQUALS,

    /**
     * Inequality: field != value
     */
    NOT_EQUALS,

    /**
     * Greater than: field > value
     */
    GREATER_THAN,

    /**
     * Greater than or equal: field >= value
     */
    GREATER_THAN_OR_EQUAL,

    /**
     * Less than: field < value
     */
    LESS_THAN,

    /**
     * Less than or equal: field <= value
     */
    LESS_THAN_OR_EQUAL,

    /**
     * Like (case-insensitive): field LIKE %value%
     */
    LIKE,

    /**
     * Starts with (case-insensitive): field LIKE value%
     */
    STARTS_WITH,

    /**
     * Ends with (case-insensitive): field LIKE %value
     */
    ENDS_WITH,

    /**
     * In list: field IN (value1, value2, ...)
     */
    IN,

    /**
     * Not in list: field NOT IN (value1, value2, ...)
     */
    NOT_IN,

    /**
     * Is null: field IS NULL
     */
    IS_NULL,

    /**
     * Is not null: field IS NOT NULL
     */
    IS_NOT_NULL,

    /**
     * Between: field BETWEEN value1 AND value2
     */
    BETWEEN
}
