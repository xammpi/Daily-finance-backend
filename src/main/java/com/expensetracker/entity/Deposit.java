package com.expensetracker.entity;

import com.expensetracker.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Deposit entity - Rich domain model
 * Entities manage their own state through constructors and behavior methods
 */
@Entity
@Table(name = "deposits")
@Getter
@NoArgsConstructor // For JPA
public class Deposit extends BaseEntity {

    @Column(nullable = false, precision = 15, scale = 2)
    @Setter(AccessLevel.PRIVATE)
    private BigDecimal amount;

    @Column(nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private LocalDate date;

    @Column(length = 500)
    @Setter(AccessLevel.PRIVATE)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private User user;

    /**
     * Constructor for creating new deposit
     */
    public Deposit(BigDecimal amount, LocalDate date, String description, User user) {
        validateAmount(amount);
        validateDate(date);
        validateDescription(description);
        validateUser(user);

        this.amount = amount;
        this.date = date;
        this.description = description != null ? description.trim() : null;
        this.user = user;
    }

    /**
     * Record deposit and credit to wallet
     * @param wallet wallet to credit
     * @throws BadRequestException if wallet is null
     */
    public void creditToWallet(Wallet wallet) {
        validateWallet(wallet);
        wallet.deposit(this.amount);
    }

    /**
     * Validate deposit amount
     * @param amount amount to validate
     * @throws BadRequestException if amount is invalid
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException("Deposit amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Deposit amount must be greater than zero");
        }
    }

    /**
     * Validate wallet
     * @param wallet wallet to validate
     * @throws BadRequestException if wallet is null
     */
    private void validateWallet(Wallet wallet) {
        if (wallet == null) {
            throw new BadRequestException("Wallet cannot be null");
        }
    }

    /**
     * Validate deposit date
     * @param date date to validate
     * @throws BadRequestException if date is invalid
     */
    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new BadRequestException("Deposit date cannot be null");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new BadRequestException("Deposit date cannot be in the future");
        }
    }

    /**
     * Validate deposit description
     * @param description description to validate
     * @throws BadRequestException if description is invalid
     */
    private void validateDescription(String description) {
        if (description != null && description.trim().length() > 500) {
            throw new BadRequestException("Deposit description cannot exceed 500 characters");
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
}
