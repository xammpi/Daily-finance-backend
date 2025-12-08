package com.expensetracker.service;


import com.expensetracker.dto.common.FilterRequest;
import com.expensetracker.dto.common.PagedResponse;
import com.expensetracker.dto.transaction.TransactionRequest;
import com.expensetracker.dto.transaction.TransactionResponse;
import com.expensetracker.dto.transaction.TransactionStatisticsResponse;
import com.expensetracker.entity.*;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.TransactionMapper;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.repository.WalletRepository;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.specification.SpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    private Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById((request.categoryId()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Use rich domain model for ownership check
        if (!category.belongsToUser(userId)) {
            throw new BadRequestException("Category does not belong to current user");
        }

        // Use constructor - entity manages its own state
        Transaction transaction = new Transaction(
                request.amount(),
                request.date(),
                request.description(),
                user,
                category
        );
        transaction = transactionRepository.save(transaction);
        Wallet wallet = user.getWallet();
        if (transaction.getCategory().getType().equals(CategoryType.EXPENSE)) {
            wallet.withdraw(transaction.getAmount());
        } else wallet.deposit(transaction.getAmount());
        walletRepository.save(wallet);
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Long userId = getCurrentUserId();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        User user = transaction.getUser();

        Category category = categoryRepository.findById((request.categoryId()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Use rich domain model for ownership check
        if (!category.belongsToUser(userId)) {
            throw new BadRequestException("Category does not belong to current user");
        }

        // Use behavior method - entity manages its own state
        transaction.updateDetails(request.date(), request.description(), category);
        // Save entities
        transaction = transactionRepository.save(transaction);
        Wallet wallet = user.getWallet();
        if (transaction.getCategory().getType().equals(CategoryType.EXPENSE)) {
            wallet.withdraw(transaction.getAmount());
        } else wallet.deposit(transaction.getAmount());

        walletRepository.save(user.getWallet());

        return transactionMapper.toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        Long userId = getCurrentUserId();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new BadRequestException("Transaction does not belong to current user");
        }
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Long userId = getCurrentUserId();
        Transaction transaction = transactionRepository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // Save and delete
        transactionRepository.delete(transaction);
        Wallet wallet = transaction.getUser().getWallet();
        wallet.deposit(transaction.getAmount());
        walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public TransactionStatisticsResponse getTransactionStatistics() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        LocalDate today = LocalDate.now();

        // Calculate today's transactions
        BigDecimal todayTransaction = transactionRepository
                .getSumByUserIdAndDateBetween(userId, today, today);

        // Calculate this week's transactions (Monday to Sunday)
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
        BigDecimal weekTransactions = transactionRepository
                .getSumByUserIdAndDateBetween(userId, startOfWeek, endOfWeek);

        // Calculate this month's transactions
        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();
        BigDecimal monthTransactions = transactionRepository
                .getSumByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);

        // Calculate total transactions (all time)
        BigDecimal totalTransactions = transactionRepository.getTotalTransactionsByUserId(userId);

        // Calculate previous week's transactions
        LocalDate startOfPreviousWeek = startOfWeek.minusWeeks(1);
        LocalDate endOfPreviousWeek = endOfWeek.minusWeeks(1);
        BigDecimal previousWeekTransactions = transactionRepository
                .getSumByUserIdAndDateBetween(userId, startOfPreviousWeek, endOfPreviousWeek);

        // Calculate previous month's transactions
        YearMonth previousMonth = currentMonth.minusMonths(1);
        LocalDate startOfPreviousMonth = previousMonth.atDay(1);
        LocalDate endOfPreviousMonth = previousMonth.atEndOfMonth();
        BigDecimal previousMonthTransactions = transactionRepository
                .getSumByUserIdAndDateBetween(userId, startOfPreviousMonth, endOfPreviousMonth);

        // Calculate averages
        // Get the first transactions date to calculate average daily expenses
        List<Transaction> allTransactions = transactionRepository.findByUserId(userId);
        LocalDate firstTransactionDate = allTransactions.stream()
                .map(Transaction::getDate)
                .min(LocalDate::compareTo)
                .orElse(today);

        long daysSinceFirstTransaction = Math.max(1, DAYS.between(firstTransactionDate, today) + 1);
        BigDecimal averageDailyTransaction = totalTransactions.divide(
                BigDecimal.valueOf(daysSinceFirstTransaction), 2, HALF_UP);

        // Average weekly transactions (based on total weeks since first transaction)
        long weeksSinceFirstTransaction = Math.max(1, daysSinceFirstTransaction / 7);
        BigDecimal averageWeeklyTransactions = totalTransactions.divide(
                BigDecimal.valueOf(weeksSinceFirstTransaction), 2, HALF_UP);

        // Average monthly transactions (based on total months since first transaction)
        long monthsSinceFirstTransaction =
                Math.max(1, MONTHS.between(YearMonth.from(firstTransactionDate), YearMonth.from(today)) + 1);
        BigDecimal averageMonthlyTransactions =
                totalTransactions.divide(BigDecimal.valueOf(monthsSinceFirstTransaction), 2, HALF_UP);

        return new TransactionStatisticsResponse(
                todayTransaction,
                weekTransactions,
                monthTransactions,
                totalTransactions,
                averageDailyTransaction,
                averageWeeklyTransactions,
                averageMonthlyTransactions,
                previousWeekTransactions,
                previousMonthTransactions,
                wallet.getCurrency()
        );
    }

    /**
     * Generic search method using dynamic specifications
     * Can filter by any field using any operation
     * Example FilterRequest:
     * {
     * "criteria": [
     * { "field": "amount", "operation": "GREATER_THAN", "value": "100" },
     * { "field": "date", "operation": "BETWEEN", "value": "2024-01-01", "valueTo": "2024-12-31" },
     * { "field": "description", "operation": "LIKE", "value": "grocery" },
     * { "field": "category.name", "operation": "EQUALS", "value": "Food" }
     * ],
     * "page": 0,
     * "size": 20,
     * "sortBy": "date",
     * "sortOrder": "DESC"
     * }
     */
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> searchTransactions(FilterRequest filterRequest) {
        Long userId = getCurrentUserId();
        Specification<Transaction> spec = SpecificationBuilder.build(filterRequest);
        Specification<Transaction> userSpec = (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);

        spec = spec == null ? userSpec : spec.and(userSpec);
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 20;
        String sortBy = filterRequest.getSortBy() != null && !filterRequest.getSortBy().isBlank()
                ? filterRequest.getSortBy()
                : "date";
        Sort.Direction direction = filterRequest.getSortOrder() != null && filterRequest.getSortOrder() == com.expensetracker.dto.common.SortOrder.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Transaction> transactionPage = transactionRepository.findAll(spec, pageable);

        List<TransactionResponse> transactionResponse = transactionPage.getContent()
                .stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());

        return PagedResponse.of(
                transactionResponse,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements()
        );
    }

}
