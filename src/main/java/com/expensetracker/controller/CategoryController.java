package com.expensetracker.controller;

import com.expensetracker.dto.category.CategoryRequest;
import com.expensetracker.dto.category.CategoryResponse;
import com.expensetracker.dto.common.FilterRequest;
import com.expensetracker.dto.common.PagedResponse;
import com.expensetracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @GetMapping
    @Operation(summary = "Get all user categories")
    public ResponseEntity<List<CategoryResponse>> getUserCategories() {
        return ResponseEntity.ok(categoryService.getUserCategories());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a category")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    @Operation(
            summary = "Advanced search with dynamic criteria",
            description = """
                    Generic search endpoint supporting any field, any operation (EQUALS, LIKE, etc.), \
                    pagination, and sorting. All criteria are combined with AND logic.
                    
                    Supported operations:
                    - EQUALS, NOT_EQUALS
                    - LIKE (contains), STARTS_WITH, ENDS_WITH
                    - IS_NULL, IS_NOT_NULL
                    
                    Example request:
                    ```json
                    {
                      "criteria": [
                        { "field": "name", "operation": "LIKE", "value": "food" },
                        { "field": "description", "operation": "LIKE", "value": "daily" }
                      ],
                      "page": 0,
                      "size": 20,
                      "sortBy": "name",
                      "sortOrder": "ASC"
                    }
                    ```"""
    )
    public ResponseEntity<PagedResponse<CategoryResponse>> searchCategories(
            @Valid @RequestBody FilterRequest filterRequest) {
        return ResponseEntity.ok(categoryService.searchCategories(filterRequest));
    }
}
