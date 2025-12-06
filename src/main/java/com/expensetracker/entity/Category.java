package com.expensetracker.entity;

import com.expensetracker.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Category entity - Rich domain model
 * Entities manage their own state through constructors and behavior methods
 */
@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor // For JPA
public class Category extends BaseEntity {

    @Column(nullable = false, length = 100)
    @Setter(AccessLevel.PRIVATE)
    private String name;

    @Column(length = 255)
    @Setter(AccessLevel.PRIVATE)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Setter(AccessLevel.PRIVATE)
    private User user;

    /**
     * Constructor for creating new category
     */
    public Category(String name, String description, User user) {
        validateName(name);
        validateDescription(description);
        validateUser(user);

        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.user = user;
    }

    /**
     * Update category details with validation
     * @param newName new category name
     * @param newDescription new category description
     * @throws BadRequestException if name is invalid
     */
    public void updateDetails(String newName, String newDescription) {
        validateName(newName);
        validateDescription(newDescription);

        this.name = newName.trim();
        this.description = newDescription != null ? newDescription.trim() : null;
    }


    /**
     * Check if category belongs to user
     * @param userId user ID to check
     * @return true if category belongs to user
     */
    public boolean belongsToUser(Long userId) {
        return this.user != null && this.user.getId().equals(userId);
    }

    /**
     * Validate category name
     * @param categoryName name to validate
     * @throws BadRequestException if name is invalid
     */
    private void validateName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new BadRequestException("Category name cannot be empty");
        }
        if (categoryName.trim().length() > 100) {
            throw new BadRequestException("Category name cannot exceed 100 characters");
        }
    }

    /**
     * Validate category description
     * @param description description to validate
     * @throws BadRequestException if description is invalid
     */
    private void validateDescription(String description) {
        if (description != null && description.trim().length() > 255) {
            throw new BadRequestException("Category description cannot exceed 255 characters");
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
