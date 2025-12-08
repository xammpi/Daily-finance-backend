package com.expensetracker.dto.category;

import com.expensetracker.entity.CategoryType;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        CategoryType type
) {

}
