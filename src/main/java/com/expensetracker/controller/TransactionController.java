package com.expensetracker.controller;

import com.expensetracker.dto.common.FilterRequest;
import com.expensetracker.dto.transaction.TransactionRequest;
import com.expensetracker.dto.transaction.TransactionResponse;
import com.expensetracker.dto.transaction.TransactionSearchResponse;
import com.expensetracker.dto.transaction.TransactionStatisticsResponse;
import com.expensetracker.dto.transaction.statistics.*;
import com.expensetracker.entity.CategoryType;
import com.expensetracker.service.TransactionService;
import com.expensetracker.service.TransactionStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transactions management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionStatisticsService statisticsService;

    @PostMapping
    @Operation(summary = "Create a new transaction")
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by id")
    public ResponseEntity<TransactionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an transaction")
    public ResponseEntity<TransactionResponse> update(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an transaction")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @Deprecated
    @Operation(
            summary = "Get transaction statistics (DEPRECATED)",
            description = "Deprecated: Use /statistics/overview instead. " +
                    "This endpoint returns only expense statistics for today, week, and month. " +
                    "The new /statistics/overview endpoint provides both income and expense data."
    )
    public ResponseEntity<TransactionStatisticsResponse> getStatistics() {
        return ResponseEntity.ok(transactionService.getTransactionStatistics());
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
                    ```

                    Response includes a summary with:
                    - totalAmount: Sum of all matching transactions
                    - transactionCount: Number of matching transactions
                    - totalExpenseAmount: Sum of expense transactions
                    - totalIncomeAmount: Sum of income transactions
                    """
    )
    public ResponseEntity<TransactionSearchResponse> search(@Valid @RequestBody FilterRequest filterRequest) {
        return ResponseEntity.ok(transactionService.searchTransactions(filterRequest));
    }

    @GetMapping("/statistics/overview")
    @Operation(
            summary = "Get quick statistics overview",
            description = "Returns today, week, and month totals for both income and expenses"
    )
    public ResponseEntity<TransactionOverviewResponse> getOverview() {
        return ResponseEntity.ok(statisticsService.getOverview());
    }

    @GetMapping("/statistics/summary")
    @Operation(
            summary = "Get comprehensive summary with averages and comparisons",
            description = """
                    Supports predefined periods (TODAY, WEEK, MONTH, YEAR, ALL_TIME) \
                    and optional comparison with previous period.

                    Examples:
                    - GET /statistics/summary?period=MONTH
                    - GET /statistics/summary?period=WEEK&compareWithPrevious=true
                    """
    )
    public ResponseEntity<TransactionSummaryResponse> getSummary(
            @RequestParam(defaultValue = "MONTH") StatisticsPeriod period,
            @RequestParam(defaultValue = "false") Boolean compareWithPrevious
    ) {
        return ResponseEntity.ok(statisticsService.getSummary(period, compareWithPrevious));
    }

    @GetMapping("/statistics/range")
    @Operation(
            summary = "Get statistics for custom date range",
            description = """
                    Supports filtering by transaction type (INCOME, EXPENSE, ALL) \
                    and optional comparison with previous period.

                    Examples:
                    - GET /statistics/range?startDate=2024-01-01&endDate=2024-12-31
                    - GET /statistics/range?startDate=2024-01-01&endDate=2024-12-31&type=EXPENSE&compareWithPrevious=true
                    """
    )
    public ResponseEntity<RangeStatisticsResponse> getRangeStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(defaultValue = "false") Boolean compareWithPrevious
    ) {
        return ResponseEntity.ok(statisticsService.getRangeStatistics(
                startDate, endDate, type, compareWithPrevious));
    }

    @GetMapping("/statistics/categories")
    @Operation(
            summary = "Get category-wise breakdown with percentages",
            description = """
                    Supports filtering by period, transaction type, and minimum percentage. \
                    Use period=CUSTOM to specify custom date range.

                    Examples:
                    - GET /statistics/categories?period=MONTH&type=EXPENSE
                    - GET /statistics/categories?period=CUSTOM&startDate=2024-01-01&endDate=2024-12-31&minPercentage=5
                    """
    )
    public ResponseEntity<CategoryStatisticsResponse> getCategoryStatistics(
            @RequestParam(defaultValue = "MONTH") StatisticsPeriod period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false) BigDecimal minPercentage
    ) {
        return ResponseEntity.ok(statisticsService.getCategoryStatistics(
                period, startDate, endDate, type, minPercentage));
    }

    @GetMapping("/statistics/trends")
    @Operation(
            summary = "Get time-series trends",
            description = """
                    Returns aggregated statistics grouped by day, week, or month. \
                    Supports filtering by transaction type.

                    Examples:
                    - GET /statistics/trends?startDate=2024-11-11&endDate=2024-12-11&groupBy=DAY
                    - GET /statistics/trends?startDate=2024-01-01&endDate=2024-12-31&groupBy=MONTH&type=EXPENSE
                    """
    )
    public ResponseEntity<TrendsResponse> getTrends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAY") TrendGrouping groupBy,
            @RequestParam(required = false) CategoryType type
    ) {
        return ResponseEntity.ok(statisticsService.getTrends(
                startDate, endDate, groupBy, type));
    }
}
