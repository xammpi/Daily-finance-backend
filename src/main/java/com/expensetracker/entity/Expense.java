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
 * Expense entity - Rich domain model
 * Entities manage their own state through constructors and behavior methods
 */
@Entity
@Table(name = "expenses")
@Getter
@NoArgsConstructor // For JPA
public class Expense extends BaseEntity {

    @Column(nullable = false, precision = 10, scale = 2)
    @Setter(AccessLevel.PRIVATE)
    private BigDecimal amount;

    @Column(nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private LocalDate date;

    @Column(length = 255)
    @Setter(AccessLevel.PRIVATE)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private Category category;

    /**
     * Constructor for creating new expense
     */
    public Expense(BigDecimal amount, LocalDate date, String description, User user, Category category) {
        validateAmount(amount);
        validateDate(date);
        validateDescription(description);
        validateUser(user);
        validateCategory(category);

        this.amount = amount;
        this.date = date;
        this.description = description != null ? description.trim() : null;
        this.user = user;
        this.category = category;
    }

    /**
     * Update expense details (except amount)
     */
    public void updateDetails(LocalDate newDate, String newDescription, Category newCategory) {
        validateDate(newDate);
        validateDescription(newDescription);
        validateCategory(newCategory);

        this.date = newDate;
        this.description = newDescription != null ? newDescription.trim() : null;
        this.category = newCategory;
    }

    /**
     * Create a new expense and charge it to the wallet
     * @param wallet wallet to charge
     * @throws BadRequestException if wallet cannot afford the expense
     */
    public void chargeToWallet(Wallet wallet) {
        validateWallet(wallet);
        wallet.chargeExpense(this.amount);
    }

    /**
     * Update expense amount with wallet adjustment
     * @param newAmount new expense amount
     * @param wallet wallet to adjust
     * @throws BadRequestException if validation fails
     */
    public void updateAmount(BigDecimal newAmount, Wallet wallet) {
        validateWallet(wallet);
        validateAmount(newAmount);

        // Refund old amount
        wallet.refundExpense(this.amount);

        // Charge new amount
        wallet.chargeExpense(newAmount);

        // Update expense using private setter
        setAmount(newAmount);
    }

    /**
     * Delete expense and refund to wallet
     * @param wallet wallet to refund to
     */
    public void refundToWallet(Wallet wallet) {
        validateWallet(wallet);
        wallet.refundExpense(this.amount);
    }

    /**
     * Check if expense belongs to a user
     * @param userId user ID to check
     * @return true if expense belongs to user
     */
    public boolean belongsToUser(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }

    /**
     * Validate expense amount
     * @param amount amount to validate
     * @throws BadRequestException if amount is invalid
     */
    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException("Expense amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Expense amount must be greater than zero");
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
     * Validate expense date
     * @param date date to validate
     * @throws BadRequestException if date is invalid
     */
    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new BadRequestException("Expense date cannot be null");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new BadRequestException("Expense date cannot be in the future");
        }
    }

    /**
     * Validate expense description
     * @param description description to validate
     * @throws BadRequestException if description is invalid
     */
    private void validateDescription(String description) {
        if (description != null && description.trim().length() > 255) {
            throw new BadRequestException("Expense description cannot exceed 255 characters");
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
     * Validate category
     * @param category category to validate
     * @throws BadRequestException if category is null
     */
    private void validateCategory(Category category) {
        if (category == null) {
            throw new BadRequestException("Category cannot be null");
        }
    }
}
