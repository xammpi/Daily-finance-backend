package com.expensetracker.controller;

import com.expensetracker.dto.common.PagedResponse;
import com.expensetracker.dto.expense.*;
import com.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    @GetMapping
    @Operation(summary = "Get all user expenses")
    public ResponseEntity<List<ExpenseResponse>> getUserExpenses() {
        return ResponseEntity.ok(expenseService.getUserExpenses());
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

    @GetMapping("/filter")
    @Operation(
            summary = "Filter and paginate expenses",
            description = "Filter expenses by category, date range, amount range with pagination support. " +
                    "Results are sorted by date (newest first)."
    )
    public ResponseEntity<PagedResponse<ExpenseResponse>> filterExpenses(
            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Start date (inclusive) in YYYY-MM-DD format")
            @RequestParam(required = false) LocalDate startDate,

            @Parameter(description = "End date (inclusive) in YYYY-MM-DD format")
            @RequestParam(required = false) LocalDate endDate,

            @Parameter(description = "Minimum amount (inclusive)")
            @RequestParam(required = false) BigDecimal minAmount,

            @Parameter(description = "Maximum amount (inclusive)")
            @RequestParam(required = false) BigDecimal maxAmount,

            @Parameter(description = "Page number (0-indexed, default: 0)")
            @RequestParam(required = false, defaultValue = "0") Integer page,

            @Parameter(description = "Page size (default: 10)")
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        ExpenseFilterRequest filter = new ExpenseFilterRequest(
                categoryId,
                startDate,
                endDate,
                minAmount,
                maxAmount,
                page,
                size
        );

        return ResponseEntity.ok(expenseService.filterExpenses(filter));
    }
}
