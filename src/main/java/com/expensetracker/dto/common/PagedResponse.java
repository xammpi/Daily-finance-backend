package com.expensetracker.dto.common;

import java.util.List;

public record PagedResponse<T>(
        List<T> content,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PagedResponse<T> of(
            List<T> content,
            int currentPage,
            int pageSize,
            long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean isFirst = currentPage == 0;
        boolean isLast = currentPage >= totalPages - 1;
        boolean hasNext = currentPage < totalPages - 1;
        boolean hasPrevious = currentPage > 0;

        return new PagedResponse<>(
                content,
                currentPage,
                pageSize,
                totalElements,
                totalPages,
                isFirst,
                isLast,
                hasNext,
                hasPrevious
        );
    }
}
