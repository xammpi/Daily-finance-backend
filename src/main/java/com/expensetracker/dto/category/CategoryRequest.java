package com.expensetracker.dto.category;

import com.expensetracker.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Category type is required")
    private Category.CategoryType type;

    private String icon;

    private String color;

    private String description;

    private Long parentId;
}
