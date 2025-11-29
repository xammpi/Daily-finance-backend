package com.expensetracker.dto.category;

import com.expensetracker.entity.Category;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private Category.CategoryType type;
    private String icon;
    private String color;
    private String description;
    private Long parentId;
    private String parentName;
    private List<CategoryResponse> subcategories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
