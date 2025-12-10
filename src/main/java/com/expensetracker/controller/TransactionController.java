package com.expensetracker.controller;

import com.expensetracker.dto.common.FilterRequest;
import com.expensetracker.dto.transaction.TransactionRequest;
import com.expensetracker.dto.transaction.TransactionResponse;
import com.expensetracker.dto.transaction.TransactionSearchResponse;
import com.expensetracker.dto.transaction.TransactionStatisticsResponse;
import com.expensetracker.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transactions management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class TransactionController {

    private final TransactionService transactionService;

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
    @Operation(summary = "Get transaction statistics (today, week, month)")
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
}
