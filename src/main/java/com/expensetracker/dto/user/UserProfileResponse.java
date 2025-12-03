package com.expensetracker.dto.user;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        Long currencyId
) {
}
