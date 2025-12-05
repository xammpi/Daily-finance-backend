package com.expensetracker.dto.common;

import org.springframework.data.domain.Sort;

/**
 * Sort order enum for pagination
 */
public enum SortOrder {
    ASC,
    DESC;

    /**
     * Convert to Spring Data Sort
     */
    public Sort toSpringSort(String property) {
        return this == ASC
            ? Sort.by(property).ascending()
            : Sort.by(property).descending();
    }
}
