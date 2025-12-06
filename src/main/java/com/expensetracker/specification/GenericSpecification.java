package com.expensetracker.specification;

import com.expensetracker.dto.common.SearchCriteria;
import com.expensetracker.dto.common.SearchOperation;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic JPA Specification builder
 * Works with any entity type and field
 *
 * Usage:
 * <pre>
 * SearchCriteria criteria = new SearchCriteria("amount", SearchOperation.GREATER_THAN, 100);
 * Specification&lt;Expense&gt; spec = new GenericSpecification&lt;&gt;(criteria);
 * List&lt;Expense&gt; results = repository.findAll(spec);
 * </pre>
 */
@AllArgsConstructor
public class GenericSpecification<T> implements Specification<T> {

    private SearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        String field = criteria.getField();
        SearchOperation operation = criteria.getOperation();
        Object value = criteria.getValue();
        Object valueTo = criteria.getValueTo();

        // Handle nested fields (e.g., "category.name")
        String[] fieldParts = field.split("\\.");
        jakarta.persistence.criteria.Path<?> path = root;
        for (String part : fieldParts) {
            path = path.get(part);
        }

        return switch (operation) {
            case EQUALS -> builder.equal(path, castValue(value, path.getJavaType()));
            case NOT_EQUALS -> builder.notEqual(path, castValue(value, path.getJavaType()));
            case GREATER_THAN -> builder.greaterThan(path.as(Comparable.class),
                    (Comparable) castValue(value, path.getJavaType()));
            case GREATER_THAN_OR_EQUAL ->
                    builder.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) castValue(value, path.getJavaType()));
            case LESS_THAN ->
                    builder.lessThan(path.as(Comparable.class), (Comparable) castValue(value, path.getJavaType()));
            case LESS_THAN_OR_EQUAL ->
                    builder.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) castValue(value, path.getJavaType()));
            case LIKE -> builder.like(builder.lower(path.as(String.class)), "%" + value.toString().toLowerCase() + "%");
            case STARTS_WITH ->
                    builder.like(builder.lower(path.as(String.class)), value.toString().toLowerCase() + "%");
            case ENDS_WITH -> builder.like(builder.lower(path.as(String.class)), "%" + value.toString().toLowerCase());
            case IN -> {
                if (value instanceof List) {
                    yield path.in((List<?>) value);
                } else if (value instanceof String) {
                    // Split comma-separated values
                    String[] values = value.toString().split(",");
                    List<Object> list = new ArrayList<>();
                    for (String v : values) {
                        list.add(castValue(v.trim(), path.getJavaType()));
                    }
                    yield path.in(list);
                }
                yield path.in(value);
            }
            case NOT_IN -> {
                if (value instanceof List) {
                    yield builder.not(path.in((List<?>) value));
                } else if (value instanceof String) {
                    String[] values = value.toString().split(",");
                    List<Object> list = new ArrayList<>();
                    for (String v : values) {
                        list.add(castValue(v.trim(), path.getJavaType()));
                    }
                    yield builder.not(path.in(list));
                }
                yield builder.not(path.in(value));
            }
            case IS_NULL -> builder.isNull(path);
            case IS_NOT_NULL -> builder.isNotNull(path);
            case BETWEEN -> {
                if (valueTo == null) {
                    throw new IllegalArgumentException("BETWEEN operation requires both value and valueTo");
                }
                yield builder.between(
                        path.as(Comparable.class),
                        (Comparable) castValue(value, path.getJavaType()),
                        (Comparable) castValue(valueTo, path.getJavaType())
                );
            }
            default -> throw new IllegalArgumentException("Unsupported operation: " + operation);
        };
    }

    /**
     * Cast string value to the target field type
     */
    private Object castValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }

        String stringValue = value.toString();

        try {
            if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(stringValue);
            } else if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(stringValue);
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(stringValue);
            } else if (targetType == Float.class || targetType == float.class) {
                return Float.parseFloat(stringValue);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.parseBoolean(stringValue);
            } else if (targetType == LocalDate.class) {
                return LocalDate.parse(stringValue);
            } else if (targetType == LocalDateTime.class) {
                return LocalDateTime.parse(stringValue);
            } else if (targetType.isEnum()) {
                return Enum.valueOf((Class<Enum>) targetType, stringValue);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Cannot cast value '" + stringValue + "' to type " + targetType.getSimpleName(), e
            );
        }

        return value;
    }
}
