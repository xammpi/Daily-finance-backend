package com.expensetracker.dto.transaction;

import com.expensetracker.dto.common.PagedResponse;

/**
 * Response for transaction search with summary information
 */
public record TransactionSearchResponse(
        PagedResponse<TransactionResponse> transactions,
        TransactionSearchSummary summary
) {
    public static TransactionSearchResponse of(
            PagedResponse<TransactionResponse> transactions,
            TransactionSearchSummary summary
    ) {
        return new TransactionSearchResponse(transactions, summary);
    }
}
