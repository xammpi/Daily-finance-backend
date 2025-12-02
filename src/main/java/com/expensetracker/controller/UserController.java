package com.expensetracker.controller;

import com.expensetracker.dto.user.BalanceSummaryResponse;
import com.expensetracker.dto.user.DepositRequest;
import com.expensetracker.dto.user.UserProfileResponse;
import com.expensetracker.entity.Currency;
import com.expensetracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money to balance")
    public ResponseEntity<UserProfileResponse> depositMoney(@Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(userService.depositMoney(request));
    }

    @PutMapping("/currency")
    @Operation(summary = "Update user currency preference")
    public ResponseEntity<UserProfileResponse> updateCurrency(@RequestParam Currency currency) {
        return ResponseEntity.ok(userService.updateCurrency(currency));
    }

    @GetMapping("/balance-summary")
    @Operation(summary = "Get balance summary with monthly expenses")
    public ResponseEntity<BalanceSummaryResponse> getBalanceSummary() {
        return ResponseEntity.ok(userService.getBalanceSummary());
    }
}
