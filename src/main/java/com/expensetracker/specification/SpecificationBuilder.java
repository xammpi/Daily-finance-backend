package com.expensetracker.specification;

import com.expensetracker.dto.common.FilterRequest;
import com.expensetracker.dto.common.SearchCriteria;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * Builder for combining multiple Specifications with AND logic
 * <p>
 * Usage:
 * <pre>
 * FilterRequest filterRequest = // ... from request
 * Specification&lt;Expense&gt; spec = SpecificationBuilder.build(filterRequest.getCriteria());
 * Page&lt;Expense&gt; results = repository.findAll(spec, pageable);
 * </pre>
 */
public class SpecificationBuilder {

    /**
     * Build a combined Specification from multiple SearchCriteria (AND logic)
     */
    public static <T> Specification<T> build(List<SearchCriteria> criteriaList) {
        if (criteriaList == null || criteriaList.isEmpty()) {
            return null;
        }

        Specification<T> specification = new GenericSpecification<>(criteriaList.getFirst());

        for (int i = 1; i < criteriaList.size(); i++) {
            specification = Specification.where(specification)
                .and(new GenericSpecification<>(criteriaList.get(i)));
        }

        return specification;
    }

    /**
     * Build a combined Specification from FilterRequest
     */
    public static <T> Specification<T> build(FilterRequest filterRequest) {
        return build(filterRequest.getCriteria());
    }

    /**
     * Combine multiple Specifications with AND logic
     */
    @SafeVarargs
    public static <T> Specification<T> and(Specification<T>... specifications) {
        Specification<T> result = null;
        for (Specification<T> spec : specifications) {
            if (spec != null) {
                result = result == null ? spec : result.and(spec);
            }
        }
        return result;
    }

    /**
     * Combine multiple Specifications with OR logic
     */
    @SafeVarargs
    public static <T> Specification<T> or(Specification<T>... specifications) {
        Specification<T> result = null;
        for (Specification<T> spec : specifications) {
            if (spec != null) {
                result = result == null ? spec : result.or(spec);
            }
        }
        return result;
    }
}
