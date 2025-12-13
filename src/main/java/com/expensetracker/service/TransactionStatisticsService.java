package com.expensetracker.service;

import com.expensetracker.dto.currency.CurrencyResponse;
import com.expensetracker.dto.transaction.statistics.*;
import com.expensetracker.entity.CategoryType;
import com.expensetracker.entity.User;
import com.expensetracker.entity.Wallet;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.CurrencyMapper;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.service.util.DateRangeCalculator;
import com.expensetracker.service.util.DateRangeCalculator.DateRange;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionStatisticsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CurrencyMapper currencyMapper;
    private final DateRangeCalculator dateRangeCalculator;

    private Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    @Transactional(readOnly = true)
    public TransactionOverviewResponse getOverview() {
        Long userId = getCurrentUserId();
        User user = userRepository.findByIdWithWallet(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);
        YearMonth currentMonth = YearMonth.now();
        LocalDate monthStart = currentMonth.atDay(1);
        LocalDate monthEnd = currentMonth.atEndOfMonth();

        OverviewProjection statistics = transactionRepository.getOverviewStatistics(
                userId, today, weekStart, weekEnd, monthStart, monthEnd
        );

        CurrencyResponse currencyResponse = currencyMapper.toResponse(wallet.getCurrency());

        return new TransactionOverviewResponse(
                statistics.todayExpenses(),
                statistics.todayIncome(),
                statistics.weekExpenses(),
                statistics.weekIncome(),
                statistics.monthExpenses(),
                statistics.monthIncome(),
                currencyResponse
        );
    }

    @Transactional(readOnly = true)
    public TransactionSummaryResponse getSummary(
            StatisticsPeriod period,
            Boolean compareWithPrevious
    ) {
        Long userId = getCurrentUserId();
        User user = userRepository.findByIdWithWallet(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        DateRange currentRange = dateRangeCalculator.calculateRange(period);
        RangeProjection currentStats = transactionRepository.getRangeStatistics(
                userId, currentRange.startDate(), currentRange.endDate(), null
        );

        long daysCount = currentRange.getDaysCount();
        BigDecimal averageExpensePerDay = daysCount > 0
                ? currentStats.totalExpenses().divide(BigDecimal.valueOf(daysCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal averageIncomePerDay = daysCount > 0
                ? currentStats.totalIncome().divide(BigDecimal.valueOf(daysCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal averageTransactionAmount = currentStats.transactionCount() > 0
                ? currentStats.totalExpenses().add(currentStats.totalIncome())
                .divide(BigDecimal.valueOf(currentStats.transactionCount()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal netAmount = currentStats.totalIncome().subtract(currentStats.totalExpenses());

        PeriodComparisonData comparison = null;
        if (Boolean.TRUE.equals(compareWithPrevious)) {
            DateRange previousRange = dateRangeCalculator.calculatePreviousRange(currentRange);
            RangeProjection previousStats = transactionRepository.getRangeStatistics(
                    userId, previousRange.startDate(), previousRange.endDate(), null
            );
            comparison = calculateComparison(
                    currentStats.totalExpenses(),
                    currentStats.totalIncome(),
                    previousStats.totalExpenses(),
                    previousStats.totalIncome()
            );
        }

        CurrencyResponse currencyResponse = currencyMapper.toResponse(wallet.getCurrency());

        return new TransactionSummaryResponse(
                period.name(),
                currentRange.startDate(),
                currentRange.endDate(),
                currentStats.totalExpenses(),
                currentStats.totalIncome(),
                netAmount,
                currentStats.transactionCount(),
                averageExpensePerDay,
                averageIncomePerDay,
                averageTransactionAmount,
                comparison,
                currencyResponse
        );
    }

    @Transactional(readOnly = true)
    public RangeStatisticsResponse getRangeStatistics(
            LocalDate startDate,
            LocalDate endDate,
            CategoryType type,
            Boolean compareWithPrevious
    ) {
        Long userId = getCurrentUserId();
        User user = userRepository.findByIdWithWallet(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        DateRange range = new DateRange(startDate, endDate);
        RangeProjection currentStats = transactionRepository.getRangeStatistics(
                userId, startDate, endDate, type
        );

        long daysCount = range.getDaysCount();
        BigDecimal totalAmount = currentStats.totalExpenses().add(currentStats.totalIncome());
        BigDecimal averagePerDay = daysCount > 0
                ? totalAmount.divide(BigDecimal.valueOf(daysCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal netAmount = currentStats.totalIncome().subtract(currentStats.totalExpenses());

        // Get top 5 categories
        List<CategoryBreakdownProjection> categoryProjections = transactionRepository.getCategoryBreakdown(
                userId, startDate, endDate, type
        );
        List<CategoryBreakdownItem> topCategories = buildCategoryBreakdown(
                categoryProjections.stream().limit(5).collect(Collectors.toList()),
                totalAmount
        );

        PeriodComparisonData comparison = null;
        if (Boolean.TRUE.equals(compareWithPrevious)) {
            DateRange previousRange = dateRangeCalculator.calculatePreviousRange(range);
            RangeProjection previousStats = transactionRepository.getRangeStatistics(
                    userId, previousRange.startDate(), previousRange.endDate(), type
            );
            comparison = calculateComparison(
                    currentStats.totalExpenses(),
                    currentStats.totalIncome(),
                    previousStats.totalExpenses(),
                    previousStats.totalIncome()
            );
        }

        CurrencyResponse currencyResponse = currencyMapper.toResponse(wallet.getCurrency());

        return new RangeStatisticsResponse(
                startDate,
                endDate,
                (int) daysCount,
                currentStats.totalExpenses(),
                currentStats.totalIncome(),
                netAmount,
                currentStats.transactionCount(),
                averagePerDay,
                topCategories,
                comparison,
                currencyResponse
        );
    }

    @Transactional(readOnly = true)
    public CategoryStatisticsResponse getCategoryStatistics(
            StatisticsPeriod period,
            LocalDate startDate,
            LocalDate endDate,
            CategoryType type,
            BigDecimal minPercentage
    ) {
        Long userId = getCurrentUserId();
        User user = userRepository.findByIdWithWallet(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        DateRange range;
        if (period == StatisticsPeriod.CUSTOM) {
            if (startDate == null || endDate == null) {
                throw new BadRequestException("startDate and endDate are required when period=CUSTOM");
            }
            range = new DateRange(startDate, endDate);
        } else {
            range = dateRangeCalculator.calculateRange(period);
            startDate = range.startDate();
            endDate = range.endDate();
        }

        List<CategoryBreakdownProjection> categoryProjections = transactionRepository.getCategoryBreakdown(
                userId, startDate, endDate, type
        );

        BigDecimal totalAmount = categoryProjections.stream()
                .map(CategoryBreakdownProjection::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long totalTransactionCount = categoryProjections.stream()
                .map(CategoryBreakdownProjection::getTransactionCount)
                .reduce(0L, Long::sum);

        List<CategoryBreakdownItem> categories = buildCategoryBreakdown(categoryProjections, totalAmount);

        // Filter by minimum percentage if provided
        if (minPercentage != null && minPercentage.compareTo(BigDecimal.ZERO) > 0) {
            categories = categories.stream()
                    .filter(item -> item.percentage().compareTo(minPercentage) >= 0)
                    .collect(Collectors.toList());
        }

        CurrencyResponse currencyResponse = currencyMapper.toResponse(wallet.getCurrency());

        return new CategoryStatisticsResponse(
                period.name(),
                startDate,
                endDate,
                totalAmount,
                totalTransactionCount,
                categories,
                currencyResponse
        );
    }

    @Transactional(readOnly = true)
    public TrendsResponse getTrends(
            LocalDate startDate,
            LocalDate endDate,
            TrendGrouping groupBy,
            CategoryType type
    ) {
        Long userId = getCurrentUserId();
        User user = userRepository.findByIdWithWallet(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        List<TrendProjection> trendProjections;
        String typeParam = type != null ? type.name() : null;

        switch (groupBy) {
            case DAY:
                trendProjections = transactionRepository.getDailyTrends(
                        userId, startDate, endDate, type
                );
                break;
            case WEEK:
                trendProjections = transactionRepository.getWeeklyTrends(
                        userId, startDate, endDate, typeParam
                );
                break;
            case MONTH:
                trendProjections = transactionRepository.getMonthlyTrends(
                        userId, startDate, endDate, typeParam
                );
                break;
            default:
                throw new BadRequestException("Unsupported groupBy value: " + groupBy);
        }

        List<TrendDataPoint> dataPoints = trendProjections.stream()
                .map(projection -> {
                    BigDecimal netAmount = projection.getIncome().subtract(projection.getExpenses());
                    return new TrendDataPoint(
                            projection.getDate(),
                            projection.getExpenses(),
                            projection.getIncome(),
                            netAmount,
                            projection.getTransactionCount()
                    );
                })
                .collect(Collectors.toList());

        CurrencyResponse currencyResponse = currencyMapper.toResponse(wallet.getCurrency());

        return new TrendsResponse(
                startDate,
                endDate,
                groupBy.name(),
                dataPoints,
                currencyResponse
        );
    }

    private PeriodComparisonData calculateComparison(
            BigDecimal currentExpenses,
            BigDecimal currentIncome,
            BigDecimal previousExpenses,
            BigDecimal previousIncome
    ) {
        BigDecimal expensesChange = currentExpenses.subtract(previousExpenses);
        BigDecimal incomeChange = currentIncome.subtract(previousIncome);

        BigDecimal expensesChangePercent = BigDecimal.ZERO;
        if (previousExpenses.compareTo(BigDecimal.ZERO) > 0) {
            expensesChangePercent = expensesChange
                    .divide(previousExpenses, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal incomeChangePercent = BigDecimal.ZERO;
        if (previousIncome.compareTo(BigDecimal.ZERO) > 0) {
            incomeChangePercent = incomeChange
                    .divide(previousIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new PeriodComparisonData(
                previousExpenses,
                previousIncome,
                expensesChange,
                expensesChangePercent,
                incomeChange,
                incomeChangePercent
        );
    }

    private List<CategoryBreakdownItem> buildCategoryBreakdown(
            List<CategoryBreakdownProjection> projections,
            BigDecimal totalAmount
    ) {
        if (projections.isEmpty()) {
            return Collections.emptyList();
        }

        return projections.stream()
                .map(projection -> {
                    BigDecimal amount = projection.getAmount();
                    Long count = projection.getTransactionCount();

                    BigDecimal percentage = BigDecimal.ZERO;
                    if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = amount
                                .divide(totalAmount, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                    }

                    BigDecimal averageTransactionAmount = count > 0
                            ? amount.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    CategoryType categoryType = CategoryType.valueOf(projection.getCategoryType());

                    return new CategoryBreakdownItem(
                            projection.getCategoryId(),
                            projection.getCategoryName(),
                            categoryType,
                            amount,
                            count,
                            percentage,
                            averageTransactionAmount
                    );
                })
                .sorted()
                .collect(Collectors.toList());
    }
}
