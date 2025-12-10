package com.expensetracker.service;


import com.expensetracker.dto.common.FilterRequest;
import com.expensetracker.dto.common.PagedResponse;
import com.expensetracker.dto.currency.CurrencyResponse;
import com.expensetracker.dto.transaction.StatisticsSummaryProjection;
import com.expensetracker.dto.transaction.TransactionRequest;
import com.expensetracker.dto.transaction.TransactionResponse;
import com.expensetracker.dto.transaction.TransactionSearchResponse;
import com.expensetracker.dto.transaction.TransactionSearchSummary;
import com.expensetracker.dto.transaction.TransactionStatisticsResponse;
import com.expensetracker.entity.*;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.CurrencyMapper;
import com.expensetracker.mapper.TransactionMapper;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.repository.WalletRepository;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.specification.SpecificationBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import static com.expensetracker.entity.CategoryType.EXPENSE;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final CurrencyMapper currencyMapper;
    private final EntityManager entityManager;

    private Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Long userId = getCurrentUserId();
        // Use optimized query that fetches user with wallet and currency in single query
        User user = userRepository.findByIdWithWallet(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById((request.categoryId()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.belongsToUser(userId)) {
            throw new BadRequestException("Category does not belong to current user");
        }

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        // Validate sufficient balance for expense transactions
        if (category.getType() == EXPENSE && wallet.hasSufficientFunds(request.amount())) {
            throw new BadRequestException("Insufficient balance. Current balance: " +
                    wallet.getAmount() + " " + wallet.getCurrency().getCode());
        }

        Transaction transaction = new Transaction(
                request.amount(),
                request.date(),
                request.description(),
                user,
                category
        );
        transaction = transactionRepository.save(transaction);

        // Apply transaction to wallet
        wallet.applyTransaction(transaction.getAmount(), category.getType());
        walletRepository.save(wallet);

        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Long userId = getCurrentUserId();
        // Use optimized query that fetches transaction with all relations in single query
        Transaction transaction = transactionRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new BadRequestException("Transaction does not belong to current user");
        }

        User user = transaction.getUser();
        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        Category newCategory = categoryRepository.findById((request.categoryId()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!newCategory.belongsToUser(userId)) {
            throw new BadRequestException("Category does not belong to current user");
        }

        BigDecimal oldAmount = transaction.getAmount();
        CategoryType oldCategoryType = transaction.getCategory().getType();

        // Revert old transaction effect
        wallet.revertTransaction(oldAmount, oldCategoryType);

        // Validate sufficient balance for new expense transaction
        if (newCategory.getType() == EXPENSE && wallet.hasSufficientFunds(request.amount())) {
            throw new BadRequestException("Insufficient balance. Current balance: " +
                    wallet.getAmount() + " " + wallet.getCurrency().getCode());
        }

        // Apply new transaction effect
        wallet.applyTransaction(request.amount(), newCategory.getType());

        // Update transaction details
        transaction.updateDetails(request.amount(), request.date(), request.description(), newCategory);

        transaction = transactionRepository.save(transaction);
        walletRepository.save(wallet);

        return transactionMapper.toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        Long userId = getCurrentUserId();
        // Use optimized query that fetches transaction with all relations in single query
        Transaction transaction = transactionRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new BadRequestException("Transaction does not belong to current user");
        }
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Long userId = getCurrentUserId();
        // Use optimized query that fetches transaction with user and wallet in single query
        Transaction transaction = transactionRepository.findByUserIdAndIdWithWallet(userId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        Wallet wallet = transaction.getUser().getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }
        wallet.revertTransaction(transaction.getAmount(), transaction.getCategory().getType());
        transactionRepository.delete(transaction);
        walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public TransactionStatisticsResponse getTransactionStatistics() {
        Long userId = getCurrentUserId();
        // Use optimized query that fetches user with wallet and currency in single query
        User user = userRepository.findByIdWithWallet(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(MONDAY);
        LocalDate weekEnd = today.with(SUNDAY);
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        // Single batched query with type-safe DTO projection (67% reduction in DB calls)
        StatisticsSummaryProjection statistics = transactionRepository.getStatisticsSummary(
                userId, today, weekStart, weekEnd, monthStart, monthEnd, EXPENSE
        );

        CurrencyResponse currencyResponse = currencyMapper.toResponse(wallet.getCurrency());

        return new TransactionStatisticsResponse(
                statistics.todayAmount(),
                statistics.weekAmount(),
                statistics.monthAmount(),
                currencyResponse
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
    public TransactionSearchResponse searchTransactions(FilterRequest filterRequest) {
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

        // Calculate summary using efficient aggregation query (no entity loading)
        TransactionSearchSummary summary = calculateSearchSummaryWithAggregation(spec);

        PagedResponse<TransactionResponse> pagedResponse = PagedResponse.of(
                transactionResponse,
                transactionPage.getNumber(),
                transactionPage.getSize(),
                transactionPage.getTotalElements()
        );

        return TransactionSearchResponse.of(pagedResponse, summary);
    }

    /**
     * Calculate summary using JPA Criteria API aggregation (efficient - no entity loading)
     * Uses a single aggregation query instead of loading all entities
     */
    private TransactionSearchSummary calculateSearchSummaryWithAggregation(Specification<Transaction> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        Root<Transaction> root = cq.from(Transaction.class);

        // Apply the search specification as predicate
        Predicate predicate = spec.toPredicate(root, cq, cb);

        // Build aggregation query
        cq.multiselect(
                cb.coalesce(cb.sum(
                        cb.<BigDecimal>selectCase()
                                .when(cb.equal(root.get("category").get("type"), CategoryType.EXPENSE),
                                        root.get("amount"))
                                .otherwise(BigDecimal.ZERO)
                ), BigDecimal.ZERO).alias("totalExpense"),
                cb.coalesce(cb.sum(
                        cb.<BigDecimal>selectCase()
                                .when(cb.equal(root.get("category").get("type"), CategoryType.INCOME),
                                        root.get("amount"))
                                .otherwise(BigDecimal.ZERO)
                ), BigDecimal.ZERO).alias("totalIncome"),
                cb.count(root).alias("count")
        ).where(predicate);

        Tuple result = entityManager.createQuery(cq).getSingleResult();

        BigDecimal totalExpense = result.get("totalExpense", BigDecimal.class);
        BigDecimal totalIncome = result.get("totalIncome", BigDecimal.class);
        Long count = result.get("count", Long.class);
        BigDecimal totalAmount = totalExpense.add(totalIncome);

        return new TransactionSearchSummary(
                totalAmount,
                count,
                totalExpense,
                totalIncome
        );
    }

}
