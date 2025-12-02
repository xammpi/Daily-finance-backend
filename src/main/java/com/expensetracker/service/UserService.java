package com.expensetracker.service;

import com.expensetracker.dto.user.BalanceSummaryResponse;
import com.expensetracker.dto.user.DepositRequest;
import com.expensetracker.dto.user.UserProfileResponse;
import com.expensetracker.entity.Currency;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

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

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getBalance(),
                user.getCurrency()
        );
    }

    @Transactional
    public UserProfileResponse depositMoney(DepositRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Add the deposit amount to balance
        user.setBalance(user.getBalance().add(request.amount()));
        user = userRepository.save(user);

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getBalance(),
                user.getCurrency()
        );
    }

    @Transactional
    public UserProfileResponse updateCurrency(Currency currency) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setCurrency(currency);
        user = userRepository.save(user);

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getBalance(),
                user.getCurrency()
        );
    }

    @Transactional(readOnly = true)
    public BalanceSummaryResponse getBalanceSummary() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Calculate current month expenses
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        List<Expense> monthlyExpenses = expenseRepository.findByUserIdAndDateBetween(
                userId, startOfMonth, endOfMonth);

        BigDecimal totalExpensesThisMonth = monthlyExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Remaining balance is just the current balance
        // (it already accounts for all expenses)
        BigDecimal remainingBalance = user.getBalance();

        return new BalanceSummaryResponse(
                user.getBalance(),
                totalExpensesThisMonth,
                remainingBalance,
                user.getCurrency()
        );
    }
}
