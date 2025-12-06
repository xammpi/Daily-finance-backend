package com.expensetracker.controller;

import com.expensetracker.dto.common.FilterRequest;
import com.expensetracker.dto.common.PagedResponse;
import com.expensetracker.dto.expense.CategoryStatisticsResponse;
import com.expensetracker.dto.expense.ExpenseRequest;
import com.expensetracker.dto.expense.ExpenseResponse;
import com.expensetracker.dto.expense.ExpenseStatisticsResponse;
import com.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expenses management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Create a new expense")
    public ResponseEntity<ExpenseResponse> createCategory(@Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID")
    public ResponseEntity<ExpenseResponse> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an expense")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an expense")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get expense statistics (today, week, month, totals, averages, comparisons)")
    public ResponseEntity<ExpenseStatisticsResponse> getExpenseStatistics() {
        return ResponseEntity.ok(expenseService.getExpenseStatistics());
    }

    @GetMapping("/statistics/by-category")
    @Operation(summary = "Get category-wise expense statistics for a date range")
    public ResponseEntity<CategoryStatisticsResponse> getCategoryStatistics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        return ResponseEntity.ok(expenseService.getCategoryStatistics(startDate, endDate));
    }

    @PostMapping("/search")
    @Operation(
            summary = "Advanced search with dynamic criteria",
            description = """
                    Generic search endpoint supporting any field, any operation (EQUALS, LIKE, GREATER_THAN, etc.), \
                    pagination, and sorting. Supports nested fields (e.g., 'category.name'). \
                    All criteria are combined with AND logic.
                    
                    Supported operations:
                    - EQUALS, NOT_EQUALS
                    - GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL
                    - LIKE (contains), STARTS_WITH, ENDS_WITH
                    - IN, NOT_IN (comma-separated values)
                    - IS_NULL, IS_NOT_NULL
                    - BETWEEN (requires both value and valueTo)
                    
                    Example request:
                    ```json
                    {
                      "criteria": [
                        { "field": "amount", "operation": "GREATER_THAN", "value": "100" },
                        { "field": "date", "operation": "BETWEEN", "value": "2024-01-01", "valueTo": "2024-12-31" },
                        { "field": "description", "operation": "LIKE", "value": "grocery" },
                        { "field": "category.name", "operation": "EQUALS", "value": "Food" }
                      ],
                      "page": 0,
                      "size": 20,
                      "sortBy": "date",
                      "sortOrder": "DESC"
                    }
                    ```"""
    )
    public ResponseEntity<PagedResponse<ExpenseResponse>> searchExpenses(
            @Valid @RequestBody FilterRequest filterRequest) {
        return ResponseEntity.ok(expenseService.searchExpenses(filterRequest));
    }
}
