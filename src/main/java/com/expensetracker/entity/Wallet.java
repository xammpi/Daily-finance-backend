package com.expensetracker.entity;

import com.expensetracker.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Wallet entity - Rich domain model
 * Entities manage their own state through constructors and behavior methods
 */
@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor // For JPA
public class Wallet extends BaseEntity {

    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("100.00");

    @Column(nullable = false, precision = 15, scale = 2)
    @Setter(AccessLevel.PRIVATE)
    private BigDecimal amount = BigDecimal.ZERO;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @Setter(AccessLevel.PRIVATE)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "currency_id", nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private Currency currency;

    /**
     * Constructor for creating new wallet
     */
    public Wallet(User user, Currency currency) {
        validateUser(user);
        validateCurrency(currency);

        this.user = user;
        this.currency = currency;
        this.amount = BigDecimal.ZERO;
    }

    /**
     * Deposit money into wallet
     * @param depositAmount amount to add
     * @throws BadRequestException if amount is invalid
     */
    public void deposit(BigDecimal depositAmount) {
        validatePositiveAmount(depositAmount, "Deposit amount");
        setAmount(this.amount.add(depositAmount));
    }

    /**
     * Withdraw money from wallet
     * @param withdrawAmount amount to withdraw
     * @throws BadRequestException if amount is invalid or insufficient balance
     */
    public void withdraw(BigDecimal withdrawAmount) {
        validatePositiveAmount(withdrawAmount, "Withdrawal amount");
        if (!canAfford(withdrawAmount)) {
            throw new BadRequestException(
                "Insufficient balance. Current balance: " + this.amount +
                ", Required: " + withdrawAmount
            );
        }
        setAmount(this.amount.subtract(withdrawAmount));
    }

    /**
     * Charge an expense to the wallet
     * @param expenseAmount amount to charge
     * @throws BadRequestException if amount is invalid or insufficient balance
     */
    public void chargeExpense(BigDecimal expenseAmount) {
        validatePositiveAmount(expenseAmount, "Expense amount");
        if (!canAfford(expenseAmount)) {
            throw new BadRequestException(
                "Insufficient balance. Current balance: " + this.amount +
                ", Required: " + expenseAmount
            );
        }
        setAmount(this.amount.subtract(expenseAmount));
    }

    /**
     * Refund an expense to the wallet
     * @param expenseAmount amount to refund
     */
    public void refundExpense(BigDecimal expenseAmount) {
        validatePositiveAmount(expenseAmount, "Refund amount");
        setAmount(this.amount.add(expenseAmount));
    }

    /**
     * Update wallet balance directly
     * @param newBalance new balance amount
     * @throws BadRequestException if new balance is negative
     */
    public void updateBalance(BigDecimal newBalance) {
        if (newBalance == null || newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Balance cannot be negative");
        }
        setAmount(newBalance);
    }

    /**
     * Change wallet currency with validation
     * @param newCurrency new currency
     * @throws BadRequestException if currency is null
     */
    public void changeCurrency(Currency newCurrency) {
        validateCurrency(newCurrency);
        this.currency = newCurrency;
    }

    /**
     * Check if wallet can afford an amount
     * @param requiredAmount amount to check
     * @return true if wallet has sufficient balance
     */
    public boolean canAfford(BigDecimal requiredAmount) {
        return requiredAmount != null && this.amount.compareTo(requiredAmount) >= 0;
    }

    /**
     * Check if wallet balance is low
     * @return true if balance is below threshold
     */
    public boolean isLowBalance() {
        return this.amount.compareTo(LOW_BALANCE_THRESHOLD) < 0;
    }

    /**
     * Validate that amount is positive
     * @param amount amount to validate
     * @param fieldName name of field for error message
     * @throws BadRequestException if amount is invalid
     */
    private void validatePositiveAmount(BigDecimal amount, String fieldName) {
        if (amount == null) {
            throw new BadRequestException(fieldName + " cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(fieldName + " must be greater than zero");
        }
    }

    /**
     * Validate user
     * @param user user to validate
     * @throws BadRequestException if user is null
     */
    private void validateUser(User user) {
        if (user == null) {
            throw new BadRequestException("User cannot be null");
        }
    }

    /**
     * Validate currency
     * @param currency currency to validate
     * @throws BadRequestException if currency is null
     */
    private void validateCurrency(Currency currency) {
        if (currency == null) {
            throw new BadRequestException("Currency cannot be null");
        }
    }

}
