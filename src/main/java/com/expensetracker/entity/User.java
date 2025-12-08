package com.expensetracker.entity;

import com.expensetracker.exception.BadRequestException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(nullable = false)
    private Boolean enabled = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Wallet wallet;

    public User(String email, String username, String password, String firstName, String lastName) {
        validateEmail(email);
        validateUsername(username);
        validatePassword(password);
        validateFirstName(firstName);
        validateLastName(lastName);

        this.email = email.trim().toLowerCase();
        this.username = username.trim();
        this.password = password;
        this.firstName = firstName != null ? firstName.trim() : null;
        this.lastName = lastName != null ? lastName.trim() : null;
        this.enabled = true;
    }

    public void addWallet(Wallet wallet) {
        this.wallet = wallet;
    }

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

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new BadRequestException("Password cannot be empty");
        }
        // Additional password strength validation should be done in the service layer before encoding
    }

    private void validateFirstName(String firstName) {
        if (firstName != null && !firstName.trim().isEmpty()) {
            if (firstName.trim().length() > 100) {
                throw new BadRequestException("First name cannot exceed 100 characters");
            }
        }
    }

    private void validateLastName(String lastName) {
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (lastName.trim().length() > 100) {
                throw new BadRequestException("Last name cannot exceed 100 characters");
            }
        }
    }
}
