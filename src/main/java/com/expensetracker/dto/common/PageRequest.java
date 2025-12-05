package com.expensetracker.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic pagination request
 * Can be used with any entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Pagination parameters")
public class PageRequest {

    @Schema(description = "Page number (0-indexed)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "Page number must be non-negative")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "Page size", example = "20", defaultValue = "20")
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    @Builder.Default
    private Integer size = 20;

    @Schema(description = "Sort field name (entity field)", example = "createdAt")
    private String sortBy;

    @Schema(description = "Sort direction", example = "DESC", defaultValue = "DESC")
    @Builder.Default
    private SortOrder sortOrder = SortOrder.DESC;

    /**
     * Convert to Spring Data PageRequest
     */
    public org.springframework.data.domain.PageRequest toSpringPageRequest() {
        if (sortBy != null && !sortBy.isBlank()) {
            return org.springframework.data.domain.PageRequest.of(
                page,
                size,
                sortOrder.toSpringSort(sortBy)
            );
        }
        return org.springframework.data.domain.PageRequest.of(page, size);
    }
}
