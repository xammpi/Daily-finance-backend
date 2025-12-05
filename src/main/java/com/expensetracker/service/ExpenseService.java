package com.expensetracker.service;


import com.expensetracker.dto.common.FilterRequest;
import com.expensetracker.dto.common.PagedResponse;
import com.expensetracker.dto.common.SearchCriteria;
import com.expensetracker.dto.common.SearchOperation;
import com.expensetracker.dto.expense.*;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.entity.Wallet;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.ExpenseMapper;
import com.expensetracker.repository.*;
import com.expensetracker.specification.SpecificationBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.expensetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;

    private Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        Category category = categoryRepository.findById((request.categoryId().longValue()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(userId)) {
            throw new BadRequestException("Category does not belong to current user");
        }

        // Check if balance is sufficient
        if (wallet.getAmount().compareTo(request.amount()) < 0) {
            throw new BadRequestException("Insufficient balance. Current balance: " + wallet.getAmount()
                    + ", Required: " + request.amount());
        }

        // Deduct expense amount from wallet balance
        wallet.subtractAmount(request.amount());

        Expense expense = expenseMapper.toEntity(request);
        expense.setUser(user);
        expense.setCategory(category);

        expense = expenseRepository.save(expense);
        walletRepository.save(wallet);

        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Long userId = getCurrentUserId();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getId().equals(userId)) {
            throw new BadRequestException("Expense does not belong to current user");
        }

        User user = expense.getUser();
        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        // Reverse the old expense amount (add it back)
        wallet.addAmount(expense.getAmount());

        // Update expense fields
        expenseMapper.updateEntityFromRequest(request, expense);

        Category category = categoryRepository.findById((request.categoryId().longValue()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(userId)) {
            throw new BadRequestException("Category does not belong to current user");
        }

        expense.setCategory(category);

        // Deduct the new expense amount
        wallet.subtractAmount(expense.getAmount());

        expense = expenseRepository.save(expense);
        walletRepository.save(wallet);

        return expenseMapper.toResponse(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getUserExpenses() {
        Long userId = getCurrentUserId();
        return expenseRepository.findByUserId(userId).stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        Long userId = getCurrentUserId();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getId().equals(userId)) {
            throw new BadRequestException("Expense does not belong to current user");
        }

        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Long userId = getCurrentUserId();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getId().equals(userId)) {
            throw new BadRequestException("Expense does not belong to current user");
        }

        User user = expense.getUser();
        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        // Add the expense amount back to wallet balance when deleting
        wallet.addAmount(expense.getAmount());

        walletRepository.save(wallet);
        expenseRepository.delete(expense);
    }

    @Transactional(readOnly = true)
    public ExpenseStatisticsResponse getExpenseStatistics() {
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
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate this week's expenses (Monday to Sunday)
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        BigDecimal weekExpenses = expenseRepository
                .findByUserIdAndDateBetween(userId, startOfWeek, endOfWeek)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate this month's expenses
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();
        BigDecimal monthExpenses = expenseRepository
                .findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total expenses (all time)
        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByUserId(userId);

        // Calculate previous week's expenses
        LocalDate startOfPreviousWeek = startOfWeek.minusWeeks(1);
        LocalDate endOfPreviousWeek = endOfWeek.minusWeeks(1);
        BigDecimal previousWeekExpenses = expenseRepository
                .findByUserIdAndDateBetween(userId, startOfPreviousWeek, endOfPreviousWeek)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate previous month's expenses
        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDate startOfPreviousMonth = previousMonth.atDay(1);
        LocalDate endOfPreviousMonth = previousMonth.atEndOfMonth();
        BigDecimal previousMonthExpenses = expenseRepository
                .findByUserIdAndDateBetween(userId, startOfPreviousMonth, endOfPreviousMonth)
                .stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate averages
        // Get the first expense date to calculate average daily expenses
        List<Expense> allExpenses = expenseRepository.findByUserId(userId);
        LocalDate firstExpenseDate = allExpenses.stream()
                .map(Expense::getDate)
                .min(LocalDate::compareTo)
                .orElse(today);

        long daysSinceFirstExpense = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(firstExpenseDate, today) + 1);
        BigDecimal averageDailyExpenses = totalExpenses.divide(
                BigDecimal.valueOf(daysSinceFirstExpense),
                2,
                java.math.RoundingMode.HALF_UP
        );

        // Average weekly expenses (based on total weeks since first expense)
        long weeksSinceFirstExpense = Math.max(1, daysSinceFirstExpense / 7);
        BigDecimal averageWeeklyExpenses = totalExpenses.divide(
                BigDecimal.valueOf(weeksSinceFirstExpense),
                2,
                java.math.RoundingMode.HALF_UP
        );

        // Average monthly expenses (based on total months since first expense)
        long monthsSinceFirstExpense = Math.max(1, java.time.temporal.ChronoUnit.MONTHS.between(
                YearMonth.from(firstExpenseDate),
                YearMonth.from(today)
        ) + 1);
        BigDecimal averageMonthlyExpenses = totalExpenses.divide(
                BigDecimal.valueOf(monthsSinceFirstExpense),
                2,
                java.math.RoundingMode.HALF_UP
        );

        return new ExpenseStatisticsResponse(
                todayExpenses,
                weekExpenses,
                monthExpenses,
                totalExpenses,
                averageDailyExpenses,
                averageWeeklyExpenses,
                averageMonthlyExpenses,
                previousWeekExpenses,
                previousMonthExpenses,
                wallet.getCurrency()
        );
    }

    @Transactional(readOnly = true)
    public CategoryStatisticsResponse getCategoryStatistics(LocalDate startDate, LocalDate endDate) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        // If no dates provided, default to current month
        if (startDate == null || endDate == null) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1);
            endDate = currentMonth.atEndOfMonth();
        }

        // Get expenses for the date range
        List<Expense> expenses = expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);

        // Calculate total for the period
        BigDecimal totalExpenses = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group by category and calculate statistics
        List<CategoryStatisticsItem> categoryBreakdown = new ArrayList<>();

        expenses.stream()
                .collect(Collectors.groupingBy(Expense::getCategory))
                .forEach((category, categoryExpenses) -> {
                    BigDecimal categoryTotal = categoryExpenses.stream()
                            .map(Expense::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                            ? categoryTotal.multiply(BigDecimal.valueOf(100))
                                    .divide(totalExpenses, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    categoryBreakdown.add(new CategoryStatisticsItem(
                            category.getId(),
                            category.getName(),
                            categoryTotal,
                            (long) categoryExpenses.size(),
                            percentage
                    ));
                });

        // Sort by total amount descending
        categoryBreakdown.sort((a, b) -> b.totalAmount().compareTo(a.totalAmount()));

        return new CategoryStatisticsResponse(
                startDate,
                endDate,
                totalExpenses,
                categoryBreakdown,
                wallet.getCurrency()
        );
    }

    /**
     * Generic search method using dynamic specifications
     * Can filter by any field using any operation
     *
     * Example FilterRequest:
     * {
     *   "criteria": [
     *     { "field": "amount", "operation": "GREATER_THAN", "value": "100" },
     *     { "field": "date", "operation": "BETWEEN", "value": "2024-01-01", "valueTo": "2024-12-31" },
     *     { "field": "description", "operation": "LIKE", "value": "grocery" },
     *     { "field": "category.name", "operation": "EQUALS", "value": "Food" }
     *   ],
     *   "page": 0,
     *   "size": 20,
     *   "sortBy": "date",
     *   "sortOrder": "DESC"
     * }
     */
    @Transactional(readOnly = true)
    public PagedResponse<ExpenseResponse> searchExpenses(FilterRequest filterRequest) {
        Long userId = getCurrentUserId();

        // Build dynamic specification from criteria
        Specification<Expense> spec = SpecificationBuilder.build(filterRequest);

        // Always add user filter
        Specification<Expense> userSpec = (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);

        // Combine specifications
        spec = spec == null ? userSpec : spec.and(userSpec);

        // Create pageable
        Pageable pageable = filterRequest.toPageRequest().toSpringPageRequest();

        // If no sort specified, default to date descending
        if (filterRequest.getSortBy() == null || filterRequest.getSortBy().isBlank()) {
            pageable = PageRequest.of(
                    filterRequest.getPage(),
                    filterRequest.getSize(),
                    Sort.by(Sort.Direction.DESC, "date")
            );
        }

        // Execute query
        Page<Expense> expensePage = expenseRepository.findAll(spec, pageable);

        // Map to DTOs
        List<ExpenseResponse> expenseResponses = expensePage.getContent()
                .stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());

        // Return paged response
        return PagedResponse.of(
                expenseResponses,
                expensePage.getNumber(),
                expensePage.getSize(),
                expensePage.getTotalElements()
        );
    }

}
