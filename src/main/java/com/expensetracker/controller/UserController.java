package com.expensetracker.controller;

import com.expensetracker.dto.user.UserProfileResponse;
import com.expensetracker.dto.user.WalletResponse;
import com.expensetracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "User profile and balance management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        return ResponseEntity.ok(userService.getUserProfile());
    }

    @PutMapping("/currency")
    @Operation(summary = "Update user currency preference")
    public ResponseEntity<UserProfileResponse> updateCurrency(@RequestParam Long currencyId) {
        return ResponseEntity.ok(userService.updateCurrency(currencyId));
    }

    @GetMapping("/wallet")
    @Operation(summary = "Get detailed wallet information")
    public ResponseEntity<WalletResponse> getWalletDetails() {
        return ResponseEntity.ok(userService.getWalletDetails());
    }
}
