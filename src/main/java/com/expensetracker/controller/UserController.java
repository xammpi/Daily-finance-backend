package com.expensetracker.controller;

import com.expensetracker.dto.user.*;
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
    @Operation(summary = "Deposit money to wallet")
    public ResponseEntity<UserProfileResponse> depositMoney(@Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(userService.depositMoney(request));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money from wallet")
    public ResponseEntity<UserProfileResponse> withdrawMoney(@Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.ok(userService.withdrawMoney(request));
    }

    @PutMapping("/currency")
    @Operation(summary = "Update user currency preference")
    public ResponseEntity<UserProfileResponse> updateCurrency(@RequestParam Long currencyId) {
        return ResponseEntity.ok(userService.updateCurrency(currencyId));
    }

    @PutMapping("/balance")
    @Operation(summary = "Update wallet balance directly")
    public ResponseEntity<UserProfileResponse> updateBalance(@Valid @RequestBody UpdateBalanceRequest request) {
        return ResponseEntity.ok(userService.updateBalance(request));
    }

    @GetMapping("/balance-summary")
    @Operation(summary = "Get balance summary with expense statistics")
    public ResponseEntity<BalanceSummaryResponse> getBalanceSummary() {
        return ResponseEntity.ok(userService.getBalanceSummary());
    }

    @GetMapping("/wallet")
    @Operation(summary = "Get detailed wallet information")
    public ResponseEntity<WalletResponse> getWalletDetails() {
        return ResponseEntity.ok(userService.getWalletDetails());
    }
}
