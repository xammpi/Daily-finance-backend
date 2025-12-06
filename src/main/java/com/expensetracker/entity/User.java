package com.expensetracker.entity;

import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity - Rich domain model
 * Entities manage their own state through constructors and behavior methods
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor // For JPA
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    @Setter(AccessLevel.PRIVATE)
    private String email;

    @Column(nullable = false, unique = true, length = 50)
    @Setter(AccessLevel.PRIVATE)
    private String username;

    @Column(nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private String password;

    @Column(length = 100)
    @Setter(AccessLevel.PRIVATE)
    private String firstName;

    @Column(length = 100)
    @Setter(AccessLevel.PRIVATE)
    private String lastName;

    @Column(nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private Boolean enabled = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.PRIVATE)
    private Wallet wallet;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.PRIVATE)
    private List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.PRIVATE)
    private List<Expense> expenses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter(AccessLevel.PRIVATE)
    private List<Deposit> deposits = new ArrayList<>();

    /**
     * Constructor for creating new user
     */
    public User(String email, String username, String password, String firstName, String lastName) {
        validateEmail(email);
        validateUsername(username);
        validatePassword(password);
        validateFirstName(firstName);
        validateLastName(lastName);

        this.email = email.trim().toLowerCase();
        this.username = username.trim();
        this.password = password; // Already encoded by service
        this.firstName = firstName != null ? firstName.trim() : null;
        this.lastName = lastName != null ? lastName.trim() : null;
        this.enabled = true;
    }

    /**
     * Add wallet to user
     */
    public void addWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    /**
     * Create expense for this user
     * Charges the expense to the user's wallet
     * @param expense expense to create (already associated with this user via constructor)
     * @throws BadRequestException if wallet is null or insufficient balance
     */
    public void createExpense(Expense expense) {
        validateWalletExists();
        expense.chargeToWallet(this.wallet);
    }

    /**
     * Update an existing expense
     * @param expense expense to update
     * @param newAmount new amount
     * @throws BadRequestException if validation fails
     */
    public void updateExpense(Expense expense, BigDecimal newAmount) {
        validateWalletExists();
        validateExpenseOwnership(expense);
        expense.updateAmount(newAmount, this.wallet);
    }

    /**
     * Delete an expense
     * Refunds the expense amount to the user's wallet
     * @param expense expense to delete
     * @throws BadRequestException if validation fails
     */
    public void deleteExpense(Expense expense) {
        validateWalletExists();
        validateExpenseOwnership(expense);
        expense.refundToWallet(this.wallet);
    }

    /**
     * Withdraw money from wallet
     * @param amount amount to withdraw
     * @throws BadRequestException if wallet is null, amount invalid, or insufficient balance
     */
    public void withdrawFromWallet(BigDecimal amount) {
        validateWalletExists();
        this.wallet.withdraw(amount);
    }

    /**
     * Update wallet balance directly
     * @param newBalance new balance
     * @throws BadRequestException if wallet is null or balance invalid
     */
    public void updateWalletBalance(BigDecimal newBalance) {
        validateWalletExists();
        this.wallet.updateBalance(newBalance);
    }

    /**
     * Check if wallet balance is low
     * @return true if balance is low
     */
    public boolean hasLowBalance() {
        return this.wallet != null && this.wallet.isLowBalance();
    }

    /**
     * Validate that wallet exists
     * @throws ResourceNotFoundException if wallet is null
     */
    private void validateWalletExists() {
        if (this.wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }
    }

    /**
     * Validate that expense belongs to this user
     * @param expense expense to validate
     * @throws BadRequestException if expense doesn't belong to user
     */
    private void validateExpenseOwnership(Expense expense) {
        if (expense == null || !expense.belongsToUser(this.getId())) {
            throw new BadRequestException("Expense does not belong to current user");
        }
    }

    /**
     * Validate email format and constraints
     * @param email email to validate
     * @throws BadRequestException if email is invalid
     */
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("Email cannot be empty");
        }
        String trimmedEmail = email.trim();
        if (trimmedEmail.length() > 100) {
            throw new BadRequestException("Email cannot exceed 100 characters");
        }
        // Basic email format validation
        if (!trimmedEmail.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new BadRequestException("Email format is invalid");
        }
    }

    /**
     * Validate username constraints
     * @param username username to validate
     * @throws BadRequestException if username is invalid
     */
    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }
        String trimmedUsername = username.trim();
        if (trimmedUsername.length() < 3) {
            throw new BadRequestException("Username must be at least 3 characters");
        }
        if (trimmedUsername.length() > 50) {
            throw new BadRequestException("Username cannot exceed 50 characters");
        }
        // Username should contain only alphanumeric characters, underscores, and hyphens
        if (!trimmedUsername.matches("^[A-Za-z0-9_-]+$")) {
            throw new BadRequestException("Username can only contain letters, numbers, underscores, and hyphens");
        }
    }

    /**
     * Validate password constraints
     * Note: Password is already encoded when this is called, so we just check for null
     * @param password password to validate
     * @throws BadRequestException if password is invalid
     */
    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }
        // Additional password strength validation should be done in the service layer before encoding
    }

    /**
     * Validate first name constraints
     * @param firstName first name to validate
     * @throws BadRequestException if first name is invalid
     */
    private void validateFirstName(String firstName) {
        if (firstName != null && !firstName.trim().isEmpty()) {
            if (firstName.trim().length() > 100) {
                throw new BadRequestException("First name cannot exceed 100 characters");
            }
        }
    }

    /**
     * Validate last name constraints
     * @param lastName last name to validate
     * @throws BadRequestException if last name is invalid
     */
    private void validateLastName(String lastName) {
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (lastName.trim().length() > 100) {
                throw new BadRequestException("Last name cannot exceed 100 characters");
            }
        }
    }
}
