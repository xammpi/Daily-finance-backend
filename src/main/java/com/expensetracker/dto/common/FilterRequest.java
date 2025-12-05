package com.expensetracker.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic filter request containing multiple search criteria
 * All criteria are combined with AND logic
 *
 * Example request:
 * {
 *   "criteria": [
 *     { "field": "amount", "operation": "GREATER_THAN", "value": "100" },
 *     { "field": "date", "operation": "BETWEEN", "value": "2024-01-01", "valueTo": "2024-12-31" },
 *     { "field": "description", "operation": "LIKE", "value": "grocery" }
 *   ],
 *   "page": 0,
 *   "size": 20,
 *   "sortBy": "date",
 *   "sortOrder": "DESC"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Filter request with search criteria and pagination")
public class FilterRequest {

    @Schema(description = "List of search criteria (combined with AND)")
    @Valid
    @Builder.Default
    private List<SearchCriteria> criteria = new ArrayList<>();

    @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "Page size", example = "20", defaultValue = "20")
    @Builder.Default
    private Integer size = 20;

    @Schema(description = "Sort field name", example = "createdAt")
    private String sortBy;

    @Schema(description = "Sort direction", example = "DESC", defaultValue = "DESC")
    @Builder.Default
    private SortOrder sortOrder = SortOrder.DESC;

    /**
     * Convert to PageRequest
     */
    public PageRequest toPageRequest() {
        return PageRequest.builder()
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .sortOrder(sortOrder)
            .build();
    }
}
