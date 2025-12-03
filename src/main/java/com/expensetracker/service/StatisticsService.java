package com.expensetracker.service;

import com.expensetracker.dto.statistics.CategoryExpenseStatistics;
import com.expensetracker.dto.statistics.PeriodStatistics;
import com.expensetracker.dto.statistics.StatisticsResponse;
import com.expensetracker.entity.User;
import com.expensetracker.entity.Wallet;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.repository.DepositRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final UserRepository userRepository;
    private final DepositRepository depositRepository;
    private final ExpenseRepository expenseRepository;

    private Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    @Transactional(readOnly = true)
    public StatisticsResponse getOverallStatistics() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        // Get total deposits (lifetime)
        BigDecimal totalDeposits = depositRepository.getTotalDepositsByUserId(userId);

        // Get total expenses (lifetime)
        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByUserId(userId);

        // Get current month statistics
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        BigDecimal totalDepositsThisMonth = depositRepository
                .getTotalDepositsByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);

        BigDecimal totalExpensesThisMonth = expenseRepository
                .findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth)
                .stream()
                .map(expense -> expense.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new StatisticsResponse(
                wallet.getAmount(),
                totalDeposits,
                totalExpenses,
                totalDepositsThisMonth,
                totalExpensesThisMonth,
                wallet.getCurrency()
        );
    }

    @Transactional(readOnly = true)
    public List<CategoryExpenseStatistics> getCategoryWiseStatistics() {
        Long userId = getCurrentUserId();

        List<Object[]> results = expenseRepository.getCategoryWiseStatistics(userId);
        List<CategoryExpenseStatistics> statistics = new ArrayList<>();

        for (Object[] result : results) {
            Long categoryId = (Long) result[0];
            String categoryName = (String) result[1];
            BigDecimal totalAmount = (BigDecimal) result[2];
            Long expenseCount = (Long) result[3];

            statistics.add(new CategoryExpenseStatistics(
                    categoryId,
                    categoryName,
                    totalAmount,
                    expenseCount
            ));
        }

        return statistics;
    }

    @Transactional(readOnly = true)
    public PeriodStatistics getDailyStatistics() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        LocalDate today = LocalDate.now();

        // Get today's deposits
        List<com.expensetracker.entity.Deposit> deposits = depositRepository
                .findByUserIdAndDateBetween(userId, today, today);
        BigDecimal totalDeposits = deposits.stream()
                .map(com.expensetracker.entity.Deposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get today's expenses
        List<com.expensetracker.entity.Expense> expenses = expenseRepository
                .findByUserIdAndDateBetween(userId, today, today);
        BigDecimal totalExpenses = expenses.stream()
                .map(com.expensetracker.entity.Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netChange = totalDeposits.subtract(totalExpenses);

        return new PeriodStatistics(
                "daily",
                wallet.getAmount(),
                totalDeposits,
                totalExpenses,
                netChange,
                deposits.size(),
                expenses.size(),
                wallet.getCurrency()
        );
    }

    @Transactional(readOnly = true)
    public PeriodStatistics getMonthlyStatistics() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        // Get this month's deposits
        List<com.expensetracker.entity.Deposit> deposits = depositRepository
                .findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);
        BigDecimal totalDeposits = deposits.stream()
                .map(com.expensetracker.entity.Deposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get this month's expenses
        List<com.expensetracker.entity.Expense> expenses = expenseRepository
                .findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);
        BigDecimal totalExpenses = expenses.stream()
                .map(com.expensetracker.entity.Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netChange = totalDeposits.subtract(totalExpenses);

        return new PeriodStatistics(
                "monthly",
                wallet.getAmount(),
                totalDeposits,
                totalExpenses,
                netChange,
                deposits.size(),
                expenses.size(),
                wallet.getCurrency()
        );
    }

    @Transactional(readOnly = true)
    public PeriodStatistics getYearlyStatistics() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        Year currentYear = Year.now();
        LocalDate startOfYear = currentYear.atDay(1);
        LocalDate endOfYear = currentYear.atMonth(12).atEndOfMonth();

        // Get this year's deposits
        List<com.expensetracker.entity.Deposit> deposits = depositRepository
                .findByUserIdAndDateBetween(userId, startOfYear, endOfYear);
        BigDecimal totalDeposits = deposits.stream()
                .map(com.expensetracker.entity.Deposit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Get this year's expenses
        List<com.expensetracker.entity.Expense> expenses = expenseRepository
                .findByUserIdAndDateBetween(userId, startOfYear, endOfYear);
        BigDecimal totalExpenses = expenses.stream()
                .map(com.expensetracker.entity.Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netChange = totalDeposits.subtract(totalExpenses);

        return new PeriodStatistics(
                "yearly",
                wallet.getAmount(),
                totalDeposits,
                totalExpenses,
                netChange,
                deposits.size(),
                expenses.size(),
                wallet.getCurrency()
        );
    }

}
