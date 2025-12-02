package com.expensetracker.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotNull(message = "Category name can not be null")
    @NotBlank(message = "Category name is required")
    private String name;
    private String description;
}
