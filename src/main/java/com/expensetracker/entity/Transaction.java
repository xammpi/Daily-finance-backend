package com.expensetracker.entity;

import com.expensetracker.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction extends BaseEntity {

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Transaction(BigDecimal amount, LocalDate date, String description, User user, Category category) {
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

    public void updateDetails(BigDecimal newAmount, LocalDate newDate, String newDescription, Category newCategory) {
        validateAmount(newAmount);
        validateDate(newDate);
        validateDescription(newDescription);
        validateCategory(newCategory);

        this.amount = newAmount;
        this.date = newDate;
        this.description = newDescription != null ? newDescription.trim() : null;
        this.category = newCategory;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null) {
            throw new BadRequestException("Expense amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Expense amount must be greater than zero");
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new BadRequestException("Expense date cannot be null");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new BadRequestException("Expense date cannot be in the future");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.trim().length() > 255) {
            throw new BadRequestException("Expense description cannot exceed 255 characters");
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new BadRequestException("User cannot be null");
        }
    }

    private void validateCategory(Category category) {
        if (category == null) {
            throw new BadRequestException("Category cannot be null");
        }
    }
}
