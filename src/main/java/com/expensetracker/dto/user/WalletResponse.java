package com.expensetracker.dto.user;

import com.expensetracker.dto.currency.CurrencyResponse;
import com.expensetracker.entity.Currency;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(
        Long walletId,
        BigDecimal currentBalance,
        CurrencyResponse currency
) {
}
