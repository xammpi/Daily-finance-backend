package com.expensetracker.service;

import com.expensetracker.dto.user.BalanceSummaryResponse;
import com.expensetracker.dto.user.DepositRequest;
import com.expensetracker.dto.user.UserProfileResponse;
import com.expensetracker.entity.Currency;
import com.expensetracker.entity.Deposit;
import com.expensetracker.entity.User;
import com.expensetracker.entity.Wallet;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.CurrencyRepository;
import com.expensetracker.repository.DepositRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.repository.WalletRepository;
import com.expensetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CurrencyRepository currencyRepository;
    private final ExpenseRepository expenseRepository;
    private final DepositRepository depositRepository;

    private Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                wallet.getCurrency().getId()
        );
    }

    @Transactional
    public UserProfileResponse depositMoney(DepositRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        // Create deposit record
        Deposit deposit = new Deposit();
        deposit.setAmount(request.amount());
        deposit.setDate(LocalDate.now());
        deposit.setDescription("Deposit to wallet");
        deposit.setUser(user);
        depositRepository.save(deposit);

        // Update wallet balance
        wallet.addAmount(request.amount());
        walletRepository.save(wallet);

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                wallet.getCurrency().getId()
        );
    }

    @Transactional
    public UserProfileResponse updateCurrency(Long currencyId) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new BadRequestException("Currency not found"));

        wallet.setCurrency(currency);
        walletRepository.save(wallet);

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                wallet.getCurrency().getId()
        );
    }

    @Transactional(readOnly = true)
    public BalanceSummaryResponse getBalanceSummary() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        // Calculate total expenses for current month
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        BigDecimal totalExpensesThisMonth = expenseRepository
                .findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth)
                .stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BalanceSummaryResponse(
                wallet.getAmount(),
                totalExpensesThisMonth,
                wallet.getCurrency()
        );
    }

}
