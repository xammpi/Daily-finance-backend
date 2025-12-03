package com.expensetracker.controller;

import com.expensetracker.dto.statistics.CategoryExpenseStatistics;
import com.expensetracker.dto.statistics.PeriodStatistics;
import com.expensetracker.dto.statistics.StatisticsResponse;
import com.expensetracker.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Statistics and analytics endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/overall")
    @Operation(summary = "Get overall statistics", description = "Returns overall statistics including current balance, total deposits, total expenses, and monthly statistics")
    public ResponseEntity<StatisticsResponse> getOverallStatistics() {
        return ResponseEntity.ok(statisticsService.getOverallStatistics());
    }

    @GetMapping("/by-category")
    @Operation(summary = "Get category-wise expense statistics", description = "Returns expense statistics grouped by category")
    public ResponseEntity<List<CategoryExpenseStatistics>> getCategoryWiseStatistics() {
        return ResponseEntity.ok(statisticsService.getCategoryWiseStatistics());
    }

    @GetMapping("/daily")
    @Operation(summary = "Get daily statistics", description = "Returns statistics for today including deposits, expenses, and net change")
    public ResponseEntity<PeriodStatistics> getDailyStatistics() {
        return ResponseEntity.ok(statisticsService.getDailyStatistics());
    }

    @GetMapping("/monthly")
    @Operation(summary = "Get monthly statistics", description = "Returns statistics for current month including deposits, expenses, and net change")
    public ResponseEntity<PeriodStatistics> getMonthlyStatistics() {
        return ResponseEntity.ok(statisticsService.getMonthlyStatistics());
    }

    @GetMapping("/yearly")
    @Operation(summary = "Get yearly statistics", description = "Returns statistics for current year including deposits, expenses, and net change")
    public ResponseEntity<PeriodStatistics> getYearlyStatistics() {
        return ResponseEntity.ok(statisticsService.getYearlyStatistics());
    }

}
