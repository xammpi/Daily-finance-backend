package com.expensetracker.repository;

import com.expensetracker.dto.expense.ExpenseFilterRequest;
import com.expensetracker.entity.Expense;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ExpenseSpecification {

    public static Specification<Expense> filterExpenses(Long userId, ExpenseFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by user ID
            predicates.add(criteriaBuilder.equal(root.get("user").get("id"), userId));

            // Filter by category
            if (filter.categoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), filter.categoryId()));
            }

            // Filter by date range
            if (filter.startDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("date"), filter.startDate()));
            }
            if (filter.endDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("date"), filter.endDate()));
            }

            // Filter by amount range
            if (filter.minAmount() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("amount"), filter.minAmount()));
            }
            if (filter.maxAmount() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("amount"), filter.maxAmount()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
