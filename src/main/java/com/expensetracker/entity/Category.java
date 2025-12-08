package com.expensetracker.entity;

import com.expensetracker.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoryType type;

    public Category(String name, String description, CategoryType type, User user) {
        validateName(name);
        validateDescription(description);
        validateUser(user);
        validateType(type);

        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.user = user;
        this.type = type;
    }

    public void updateDetails(String newName, String newDescription, CategoryType type) {
        validateName(newName);
        validateDescription(newDescription);
        validateType(type);

        this.name = newName.trim();
        this.description = newDescription != null ? newDescription.trim() : null;
        this.type = type;
    }

    private void validateType(CategoryType type) {
        if (type == null) {
            throw new BadRequestException("Transaction type cannot be null");
        }
    }

    public boolean belongsToUser(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }

    private void validateName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new BadRequestException("Category name cannot be empty");
        }
        if (categoryName.trim().length() > 100) {
            throw new BadRequestException("Category name cannot exceed 100 characters");
        }
    }

    private void validateDescription(String description) {
        if (description != null && description.trim().length() > 255) {
            throw new BadRequestException("Category description cannot exceed 255 characters");
        }
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new BadRequestException("User cannot be null");
        }
    }
}
