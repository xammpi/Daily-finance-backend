package com.expensetracker.entity;

import com.expensetracker.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import static com.expensetracker.entity.CategoryType.EXPENSE;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "wallets")
public class Wallet extends BaseEntity {

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "currency_id", nullable = false)
    private Currency currency;

    public Wallet(User user, Currency currency) {
        validateUser(user);
        validateCurrency(currency);

        this.user = user;
        this.currency = currency;
        this.amount = BigDecimal.ZERO;
    }

    public void deposit(BigDecimal depositAmount) {
        validatePositiveAmount(depositAmount, "Deposit amount");
        this.amount = this.amount.add(depositAmount);
    }

    public void withdraw(BigDecimal withdrawAmount) {
        validatePositiveAmount(withdrawAmount, "Withdraw amount");
        this.amount = this.amount.subtract(withdrawAmount);
    }

    public void applyTransaction(BigDecimal amount, CategoryType type) {
        if (type == EXPENSE) {
            withdraw(amount);
        } else {
            deposit(amount);
        }
    }

    public void revertTransaction(BigDecimal amount, CategoryType type) {
        if (type.equals(EXPENSE)) {
            deposit(amount);
        } else {
            withdraw(amount);
        }
    }

    public boolean hasSufficientFunds(BigDecimal amount) {
        return this.amount.compareTo(amount) < 0;
    }

    public void changeCurrency(Currency newCurrency) {
        validateCurrency(newCurrency);
        this.currency = newCurrency;
    }

    private void validatePositiveAmount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new BadRequestException(fieldName + " cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(fieldName + " must be greater than zero");
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new BadRequestException("User cannot be null");
        }
    }

    private void validateCurrency(Currency currency) {
        if (currency == null) {
            throw new BadRequestException("Currency cannot be null");
        }
    }

}
