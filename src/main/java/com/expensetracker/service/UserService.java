package com.expensetracker.service;

import com.expensetracker.dto.user.*;
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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        // Use constructor - entity manages its own state
        Deposit deposit = new Deposit(
                request.amount(),
                LocalDate.now(),
                "Deposit to wallet",
                user
        );
        depositRepository.save(deposit);

        // Credit deposit to wallet using rich domain model
        deposit.creditToWallet(user.getWallet());
        walletRepository.save(user.getWallet());

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getWallet().getCurrency().getId()
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

        // Use rich domain model - wallet handles currency change
        wallet.changeCurrency(currency);
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
    public UserProfileResponse updateBalance(UpdateBalanceRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Use rich domain model - user handles wallet validation and update
        user.updateWalletBalance(request.amount());
        walletRepository.save(user.getWallet());

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getWallet().getCurrency().getId()
        );
    }

    @Transactional
    public UserProfileResponse withdrawMoney(WithdrawRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Use rich domain model - user handles wallet validation and withdrawal
        user.withdrawFromWallet(request.amount());
        walletRepository.save(user.getWallet());

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getWallet().getCurrency().getId()
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

        LocalDate today = LocalDate.now();

        // Calculate today's expenses
        BigDecimal todayExpenses = expenseRepository
                .findByUserIdAndDateBetween(userId, today, today)
                .stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate this week's expenses (Monday to Sunday)
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        BigDecimal weekExpenses = expenseRepository
                .findByUserIdAndDateBetween(userId, startOfWeek, endOfWeek)
                .stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total expenses for current month
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();
        BigDecimal monthExpenses = expenseRepository
                .findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth)
                .stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BalanceSummaryResponse(
                wallet.getAmount(),
                todayExpenses,
                weekExpenses,
                monthExpenses,
                wallet.getCurrency()
        );
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletDetails() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        // Get deposit statistics
        Long totalDeposits = (long) user.getDeposits().size();
        BigDecimal totalDepositAmount = user.getDeposits().stream()
                .map(Deposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get expense statistics
        Long totalExpenses = (long) user.getExpenses().size();
        BigDecimal totalExpenseAmount = expenseRepository.getTotalExpensesByUserId(userId);

        // Get last transaction date (most recent between deposit and expense)
        LocalDateTime lastDepositDate = user.getDeposits().stream()
                .map(d -> d.getDate().atStartOfDay())
                .max(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime lastExpenseDate = user.getExpenses().stream()
                .map(e -> e.getDate().atStartOfDay())
                .max(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime lastTransactionDate = null;
        if (lastDepositDate != null && lastExpenseDate != null) {
            lastTransactionDate = lastDepositDate.isAfter(lastExpenseDate) ? lastDepositDate : lastExpenseDate;
        } else if (lastDepositDate != null) {
            lastTransactionDate = lastDepositDate;
        } else if (lastExpenseDate != null) {
            lastTransactionDate = lastExpenseDate;
        }

        // Use rich domain model for low balance check
        boolean lowBalanceWarning = user.hasLowBalance() && wallet.getAmount().compareTo(BigDecimal.ZERO) > 0;

        return new WalletResponse(
                wallet.getId(),
                wallet.getAmount(),
                wallet.getCurrency(),
                totalDeposits,
                totalDepositAmount,
                totalExpenses,
                totalExpenseAmount,
                lastTransactionDate,
                lowBalanceWarning
        );
    }

}
