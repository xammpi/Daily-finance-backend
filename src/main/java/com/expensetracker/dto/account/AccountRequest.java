package com.expensetracker.dto.account;

import com.expensetracker.entity.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountRequest {

    @NotBlank(message = "Account name is required")
    private String name;

    @NotNull(message = "Account type is required")
    private Account.AccountType type;

    private BigDecimal balance = BigDecimal.ZERO;

    private String currency = "USD";

    private String description;

    private Boolean active = true;
}
