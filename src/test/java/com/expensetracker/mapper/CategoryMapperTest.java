package com.expensetracker.mapper;

import com.expensetracker.dto.category.CategoryRequest;
import com.expensetracker.dto.category.CategoryResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Currency;
import com.expensetracker.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {

    private CategoryMapper categoryMapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        categoryMapper = Mappers.getMapper(CategoryMapper.class);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setEnabled(true);
        testUser.setBalance(BigDecimal.ZERO);
        testUser.setCurrency(Currency.USD);
    }

    @Test
    void toEntity_WithValidRequest_MapsCorrectly() {
        // Given
        CategoryRequest request = new CategoryRequest();
        request.setName("Food");
        request.setDescription("Food expenses");

        // When
        Category category = categoryMapper.toEntity(request);

        // Then
        assertNotNull(category);
        assertEquals("Food", category.getName());
        assertEquals("Food expenses", category.getDescription());
        assertNull(category.getId()); // Should be ignored
        assertNull(category.getUser()); // Should be ignored
    }

    @Test
    void toEntity_WithNullDescription_MapsCorrectly() {
        // Given
        CategoryRequest request = new CategoryRequest();
        request.setName("Food");
        request.setDescription(null);

        // When
        Category category = categoryMapper.toEntity(request);

        // Then
        assertNotNull(category);
        assertEquals("Food", category.getName());
        assertNull(category.getDescription());
    }

    @Test
    void toResponse_WithValidCategory_MapsCorrectly() {
        // Given
        Category category = new Category();
        category.setId(1L);
        category.setName("Food");
        category.setDescription("Food expenses");
        category.setUser(testUser);
        category.setCreatedAt(LocalDateTime.of(2024, 12, 1, 10, 0));
        category.setUpdatedAt(LocalDateTime.of(2024, 12, 1, 12, 0));

        // When
        CategoryResponse response = categoryMapper.toResponse(category);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Food", response.name());
        assertEquals("Food expenses", response.description());
        assertEquals(LocalDateTime.of(2024, 12, 1, 10, 0), response.createdAt());
        assertEquals(LocalDateTime.of(2024, 12, 1, 12, 0), response.updatedAt());
    }

    @Test
    void toResponse_WithNullDescription_MapsCorrectly() {
        // Given
        Category category = new Category();
        category.setId(1L);
        category.setName("Food");
        category.setDescription(null);
        category.setUser(testUser);
        category.setCreatedAt(LocalDateTime.of(2024, 12, 1, 10, 0));
        category.setUpdatedAt(LocalDateTime.of(2024, 12, 1, 12, 0));

        // When
        CategoryResponse response = categoryMapper.toResponse(category);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Food", response.name());
        assertNull(response.description());
    }

    @Test
    void updateEntityFromRequest_WithValidRequest_UpdatesEntity() {
        // Given
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Food");
        existingCategory.setDescription("Old description");
        existingCategory.setUser(testUser);
        existingCategory.setCreatedAt(LocalDateTime.of(2024, 12, 1, 10, 0));
        existingCategory.setUpdatedAt(LocalDateTime.of(2024, 12, 1, 10, 0));

        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Food");
        request.setDescription("Updated description");

        // When
        categoryMapper.updateEntityFromRequest(request, existingCategory);

        // Then
        assertEquals("Updated Food", existingCategory.getName());
        assertEquals("Updated description", existingCategory.getDescription());
        // Ignored fields should remain unchanged
        assertEquals(1L, existingCategory.getId());
        assertEquals(testUser, existingCategory.getUser());
        assertEquals(LocalDateTime.of(2024, 12, 1, 10, 0), existingCategory.getCreatedAt());
    }

    @Test
    void updateEntityFromRequest_WithNullDescription_UpdatesEntity() {
        // Given
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Food");
        existingCategory.setDescription("Old description");
        existingCategory.setUser(testUser);

        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Food");
        request.setDescription(null);

        // When
        categoryMapper.updateEntityFromRequest(request, existingCategory);

        // Then
        assertEquals("Updated Food", existingCategory.getName());
        // With null value mapping strategy IGNORE, description should remain unchanged
        assertEquals("Old description", existingCategory.getDescription());
    }

    @Test
    void updateEntityFromRequest_DoesNotChangeIgnoredFields() {
        // Given
        User originalUser = new User();
        originalUser.setId(99L);
        originalUser.setUsername("originaluser");

        Category existingCategory = new Category();
        existingCategory.setId(123L);
        existingCategory.setName("Food");
        existingCategory.setDescription("Old description");
        existingCategory.setUser(originalUser);
        existingCategory.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        existingCategory.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));

        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Food");
        request.setDescription("Updated description");

        // When
        categoryMapper.updateEntityFromRequest(request, existingCategory);

        // Then
        // Updated fields
        assertEquals("Updated Food", existingCategory.getName());
        assertEquals("Updated description", existingCategory.getDescription());
        // Ignored fields should remain unchanged
        assertEquals(123L, existingCategory.getId());
        assertEquals(originalUser, existingCategory.getUser());
        assertEquals(99L, existingCategory.getUser().getId());
        assertEquals("originaluser", existingCategory.getUser().getUsername());
    }

    @Test
    void toEntity_WithEmptyDescription_MapsCorrectly() {
        // Given
        CategoryRequest request = new CategoryRequest();
        request.setName("Food");
        request.setDescription("");

        // When
        Category category = categoryMapper.toEntity(request);

        // Then
        assertNotNull(category);
        assertEquals("Food", category.getName());
        assertEquals("", category.getDescription());
    }

    @Test
    void toResponse_WithAllFields_MapsCorrectly() {
        // Given
        Category category = new Category();
        category.setId(42L);
        category.setName("Transport");
        category.setDescription("All transport related expenses");
        category.setUser(testUser);
        category.setCreatedAt(LocalDateTime.of(2024, 6, 15, 9, 30));
        category.setUpdatedAt(LocalDateTime.of(2024, 6, 20, 14, 45));

        // When
        CategoryResponse response = categoryMapper.toResponse(category);

        // Then
        assertEquals(42L, response.id());
        assertEquals("Transport", response.name());
        assertEquals("All transport related expenses", response.description());
        assertEquals(LocalDateTime.of(2024, 6, 15, 9, 30), response.createdAt());
        assertEquals(LocalDateTime.of(2024, 6, 20, 14, 45), response.updatedAt());
    }
}
