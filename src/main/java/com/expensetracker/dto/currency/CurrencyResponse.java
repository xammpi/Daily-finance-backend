package com.expensetracker.dto.currency;

/**
 * Response DTO for Currency
 */
public record CurrencyResponse(
        Long id,
        String code,
        String name,
        String symbol
) {
}
