package com.expensetracker.dto.user;

import com.expensetracker.entity.Currency;

import java.math.BigDecimal;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        BigDecimal balance,
        Currency currency
) {
}
