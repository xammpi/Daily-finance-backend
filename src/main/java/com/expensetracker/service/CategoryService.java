package com.expensetracker.service;

import com.expensetracker.dto.category.CategoryRequest;
import com.expensetracker.dto.category.CategoryResponse;
import com.expensetracker.dto.common.FilterRequest;
import com.expensetracker.dto.common.PagedResponse;
import com.expensetracker.dto.common.SortOrder;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.CategoryMapper;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.UserPrincipal;
import com.expensetracker.specification.SpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    private Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, request.getName())) {
            throw new BadRequestException("Category already exists");
        }

        Category category = new Category(request.getName(), request.getDescription(), request.getType(), user);

        category = categoryRepository.save(category);
        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Long userId = getCurrentUserId();
        // Use optimized query that fetches category with user in single query
        Category category = categoryRepository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Use rich domain model for ownership check
        if (!category.belongsToUser(userId)) {
            throw new BadRequestException("Category does not belong to current user");
        }

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Long userId = getCurrentUserId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (categoryRepository.existsByUserIdAndNameIgnoreCase(userId, request.getName())) {
            throw new BadRequestException("Category already exists");
        }

        if (!category.belongsToUser(userId)) {
            throw new BadRequestException("Category does not belong to current user");
        }

        category.updateDetails(request.getName(), request.getDescription(), request.getType());
        category = categoryRepository.save(category);

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Long userId = getCurrentUserId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Use rich domain model for ownership check
        if (!category.belongsToUser(userId)) {
            throw new BadRequestException("Category does not belong to current user");
        }

        categoryRepository.delete(category);
    }

    /**
     * Generic search method using dynamic specifications
     * Can filter by any field using any operation
     * <p>
     * Example FilterRequest:
     * {
     * "criteria": [
     * { "field": "name", "operation": "LIKE", "value": "food" },
     * { "field": "description", "operation": "LIKE", "value": "daily" }
     * ],
     * "page": 0,
     * "size": 20,
     * "sortBy": "name",
     * "sortOrder": "ASC"
     * }
     */
    @Transactional(readOnly = true)
    public PagedResponse<CategoryResponse> searchCategories(FilterRequest filterRequest) {
        Long userId = getCurrentUserId();

        // Build dynamic specification from criteria
        Specification<Category> spec = SpecificationBuilder.build(filterRequest);

        // Always add user filter
        Specification<Category> userSpec = (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);

        // Combine specifications
        spec = spec == null ? userSpec : spec.and(userSpec);

        // Handle pagination and sorting with defaults
        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? filterRequest.getSize() : 20;
        String sortBy = filterRequest.getSortBy() != null && !filterRequest.getSortBy().isBlank()
                ? filterRequest.getSortBy()
                : "name";
        Sort.Direction direction = filterRequest.getSortOrder() != null && filterRequest.getSortOrder() == SortOrder.DESC
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        // Create pageable with defaults
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Execute query
        Page<Category> categoryPage = categoryRepository.findAll(spec, pageable);

        // Map to DTOs
        List<CategoryResponse> categoryResponses = categoryPage.getContent()
                .stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());

        // Return paged response
        return PagedResponse.of(
                categoryResponses,
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalElements()
        );
    }
}
