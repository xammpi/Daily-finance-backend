package com.expensetracker.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single search criterion for dynamic filtering
 * Example: { "field": "amount", "operation": "GREATER_THAN", "value": "100" }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search criterion for filtering")
public class SearchCriteria {

    @Schema(description = "Field name to search on", example = "amount", required = true)
    @NotBlank(message = "Field name is required")
    private String field;

    @Schema(description = "Search operation", example = "GREATER_THAN", required = true)
    @NotNull(message = "Operation is required")
    private SearchOperation operation;

    @Schema(description = "Value to compare (can be single value, comma-separated list for IN/NOT_IN, or two values for BETWEEN)",
            example = "100")
    private Object value;

    @Schema(description = "Second value (used only for BETWEEN operation)", example = "500")
    private Object valueTo;
}
