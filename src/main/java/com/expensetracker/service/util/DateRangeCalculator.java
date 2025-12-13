package com.expensetracker.service.util;

import com.expensetracker.dto.transaction.statistics.StatisticsPeriod;
import com.expensetracker.dto.transaction.statistics.TrendGrouping;
import com.expensetracker.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Component
public class DateRangeCalculator {

    public DateRange calculateRange(StatisticsPeriod period) {
        LocalDate today = LocalDate.now();
        return switch (period) {
            case TODAY -> new DateRange(today, today);
            case WEEK -> calculateWeekRange(today);
            case MONTH -> calculateMonthRange(today);
            case YEAR -> calculateYearRange(today);
            case ALL_TIME -> new DateRange(LocalDate.of(2000, 1, 1), today);
            case CUSTOM -> throw new BadRequestException(
                    "Custom period requires explicit startDate and endDate");
        };
    }

    public DateRange calculatePreviousRange(DateRange current) {
        long days = ChronoUnit.DAYS.between(current.startDate(), current.endDate());
        LocalDate previousEnd = current.startDate().minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(days);
        return new DateRange(previousStart, previousEnd);
    }

    public List<DateRange> generateBuckets(
            LocalDate start,
            LocalDate end,
            TrendGrouping groupBy
    ) {
        List<DateRange> buckets = new ArrayList<>();

        return switch (groupBy) {
            case DAY -> generateDailyBuckets(start, end);
            case WEEK -> generateWeeklyBuckets(start, end);
            case MONTH -> generateMonthlyBuckets(start, end);
        };
    }

    private List<DateRange> generateDailyBuckets(LocalDate start, LocalDate end) {
        List<DateRange> buckets = new ArrayList<>();
        LocalDate current = start;
        while (!current.isAfter(end)) {
            buckets.add(new DateRange(current, current));
            current = current.plusDays(1);
        }
        return buckets;
    }

    private List<DateRange> generateWeeklyBuckets(LocalDate start, LocalDate end) {
        List<DateRange> buckets = new ArrayList<>();
        LocalDate current = start.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        while (!current.isAfter(end)) {
            LocalDate weekEnd = current.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            // Ensure we don't exceed the requested end date
            if (weekEnd.isAfter(end)) {
                weekEnd = end;
            }
            // Only add if the week overlaps with our requested range
            if (!current.isAfter(end)) {
                buckets.add(new DateRange(current, weekEnd));
            }
            current = weekEnd.plusDays(1);
        }
        return buckets;
    }

    private List<DateRange> generateMonthlyBuckets(LocalDate start, LocalDate end) {
        List<DateRange> buckets = new ArrayList<>();
        YearMonth currentMonth = YearMonth.from(start);
        YearMonth endMonth = YearMonth.from(end);

        while (!currentMonth.isAfter(endMonth)) {
            LocalDate monthStart = currentMonth.atDay(1);
            LocalDate monthEnd = currentMonth.atEndOfMonth();

            // Adjust first month if start date is mid-month
            if (currentMonth.equals(YearMonth.from(start))) {
                monthStart = start;
            }

            // Adjust last month if end date is mid-month
            if (currentMonth.equals(endMonth)) {
                monthEnd = end;
            }

            buckets.add(new DateRange(monthStart, monthEnd));
            currentMonth = currentMonth.plusMonths(1);
        }
        return buckets;
    }

    private DateRange calculateWeekRange(LocalDate date) {
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        LocalDate sunday = date.with(DayOfWeek.SUNDAY);
        return new DateRange(monday, sunday);
    }

    private DateRange calculateMonthRange(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        return new DateRange(
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()
        );
    }

    private DateRange calculateYearRange(LocalDate date) {
        return new DateRange(
                LocalDate.of(date.getYear(), 1, 1),
                LocalDate.of(date.getYear(), 12, 31)
        );
    }

    public record DateRange(LocalDate startDate, LocalDate endDate) {
        public DateRange {
            if (startDate.isAfter(endDate)) {
                throw new BadRequestException(
                        "Start date must be before or equal to end date");
            }
        }

        public long getDaysCount() {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
    }
}
