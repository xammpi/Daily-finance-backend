package com.expensetracker.mapper;

import com.expensetracker.dto.expense.ExpenseRequest;
import com.expensetracker.dto.expense.ExpenseResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Currency;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseMapperTest {

    private ExpenseMapper expenseMapper;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        expenseMapper = Mappers.getMapper(ExpenseMapper.class);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setEnabled(true);
        testUser.setBalance(BigDecimal.ZERO);
        testUser.setCurrency(Currency.USD);

        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Food");
        testCategory.setDescription("Food expenses");
        testCategory.setUser(testUser);
    }

    @Test
    void toEntity_WithValidRequest_MapsCorrectly() {
        // Given
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("50.00"),
                LocalDate.of(2024, 12, 1),
                "Grocery shopping",
                1
        );

        // When
        Expense expense = expenseMapper.toEntity(request);

        // Then
        assertNotNull(expense);
        assertEquals(new BigDecimal("50.00"), expense.getAmount());
        assertEquals(LocalDate.of(2024, 12, 1), expense.getDate());
        assertEquals("Grocery shopping", expense.getDescription());
        assertNull(expense.getId()); // Should be ignored
        assertNull(expense.getUser()); // Should be ignored
        assertNull(expense.getCategory()); // Should be ignored
    }

    @Test
    void toEntity_WithNullDescription_MapsCorrectly() {
        // Given
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("50.00"),
                LocalDate.of(2024, 12, 1),
                null,
                1
        );

        // When
        Expense expense = expenseMapper.toEntity(request);

        // Then
        assertNotNull(expense);
        assertEquals(new BigDecimal("50.00"), expense.getAmount());
        assertEquals(LocalDate.of(2024, 12, 1), expense.getDate());
        assertNull(expense.getDescription());
    }

    @Test
    void toResponse_WithValidExpense_MapsCorrectly() {
        // Given
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setAmount(new BigDecimal("50.00"));
        expense.setDate(LocalDate.of(2024, 12, 1));
        expense.setDescription("Grocery shopping");
        expense.setUser(testUser);
        expense.setCategory(testCategory);
        expense.setCreatedAt(LocalDateTime.of(2024, 12, 1, 10, 0));
        expense.setUpdatedAt(LocalDateTime.of(2024, 12, 1, 12, 0));

        // When
        ExpenseResponse response = expenseMapper.toResponse(expense);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(new BigDecimal("50.00"), response.amount());
        assertEquals(LocalDate.of(2024, 12, 1), response.date());
        assertEquals("Grocery shopping", response.description());
        assertEquals(1, response.categoryId());
    }

    @Test
    void toResponse_WithNullDescription_MapsCorrectly() {
        // Given
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setAmount(new BigDecimal("50.00"));
        expense.setDate(LocalDate.of(2024, 12, 1));
        expense.setDescription(null);
        expense.setUser(testUser);
        expense.setCategory(testCategory);
        expense.setCreatedAt(LocalDateTime.of(2024, 12, 1, 10, 0));
        expense.setUpdatedAt(LocalDateTime.of(2024, 12, 1, 12, 0));

        // When
        ExpenseResponse response = expenseMapper.toResponse(expense);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(new BigDecimal("50.00"), response.amount());
        assertEquals(LocalDate.of(2024, 12, 1), response.date());
        assertNull(response.description());
        assertEquals(1, response.categoryId());
    }

    @Test
    void toResponse_MapsCategoryIdCorrectly() {
        // Given
        Category category = new Category();
        category.setId(42L);
        category.setName("Transport");

        Expense expense = new Expense();
        expense.setId(1L);
        expense.setAmount(new BigDecimal("30.00"));
        expense.setDate(LocalDate.of(2024, 12, 2));
        expense.setDescription("Bus ticket");
        expense.setUser(testUser);
        expense.setCategory(category);

        // When
        ExpenseResponse response = expenseMapper.toResponse(expense);

        // Then
        assertEquals(42, response.categoryId());
    }

    @Test
    void updateEntityFromRequest_WithValidRequest_UpdatesEntity() {
        // Given
        Expense existingExpense = new Expense();
        existingExpense.setId(1L);
        existingExpense.setAmount(new BigDecimal("50.00"));
        existingExpense.setDate(LocalDate.of(2024, 12, 1));
        existingExpense.setDescription("Old description");
        existingExpense.setUser(testUser);
        existingExpense.setCategory(testCategory);
        existingExpense.setCreatedAt(LocalDateTime.of(2024, 12, 1, 10, 0));
        existingExpense.setUpdatedAt(LocalDateTime.of(2024, 12, 1, 10, 0));

        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("75.00"),
                LocalDate.of(2024, 12, 2),
                "Updated description",
                2
        );

        // When
        expenseMapper.updateEntityFromRequest(request, existingExpense);

        // Then
        assertEquals(new BigDecimal("75.00"), existingExpense.getAmount());
        assertEquals(LocalDate.of(2024, 12, 2), existingExpense.getDate());
        assertEquals("Updated description", existingExpense.getDescription());
        // Ignored fields should remain unchanged
        assertEquals(1L, existingExpense.getId());
        assertEquals(testUser, existingExpense.getUser());
        assertEquals(testCategory, existingExpense.getCategory());
        assertEquals(LocalDateTime.of(2024, 12, 1, 10, 0), existingExpense.getCreatedAt());
    }

    @Test
    void updateEntityFromRequest_WithNullDescription_UpdatesEntity() {
        // Given
        Expense existingExpense = new Expense();
        existingExpense.setId(1L);
        existingExpense.setAmount(new BigDecimal("50.00"));
        existingExpense.setDate(LocalDate.of(2024, 12, 1));
        existingExpense.setDescription("Old description");
        existingExpense.setUser(testUser);
        existingExpense.setCategory(testCategory);

        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("75.00"),
                LocalDate.of(2024, 12, 2),
                null,
                1
        );

        // When
        expenseMapper.updateEntityFromRequest(request, existingExpense);

        // Then
        assertEquals(new BigDecimal("75.00"), existingExpense.getAmount());
        assertEquals(LocalDate.of(2024, 12, 2), existingExpense.getDate());
        // With null value mapping strategy IGNORE, description should remain unchanged
        assertEquals("Old description", existingExpense.getDescription());
    }

    @Test
    void updateEntityFromRequest_DoesNotChangeIgnoredFields() {
        // Given
        User originalUser = new User();
        originalUser.setId(99L);
        originalUser.setUsername("originaluser");

        Category originalCategory = new Category();
        originalCategory.setId(88L);
        originalCategory.setName("Original Category");

        Expense existingExpense = new Expense();
        existingExpense.setId(123L);
        existingExpense.setAmount(new BigDecimal("50.00"));
        existingExpense.setDate(LocalDate.of(2024, 12, 1));
        existingExpense.setDescription("Old description");
        existingExpense.setUser(originalUser);
        existingExpense.setCategory(originalCategory);
        existingExpense.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));
        existingExpense.setUpdatedAt(LocalDateTime.of(2024, 1, 1, 10, 0));

        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("75.00"),
                LocalDate.of(2024, 12, 2),
                "Updated description",
                2
        );

        // When
        expenseMapper.updateEntityFromRequest(request, existingExpense);

        // Then
        // Updated fields
        assertEquals(new BigDecimal("75.00"), existingExpense.getAmount());
        assertEquals(LocalDate.of(2024, 12, 2), existingExpense.getDate());
        assertEquals("Updated description", existingExpense.getDescription());
        // Ignored fields should remain unchanged
        assertEquals(123L, existingExpense.getId());
        assertEquals(originalUser, existingExpense.getUser());
        assertEquals(99L, existingExpense.getUser().getId());
        assertEquals(originalCategory, existingExpense.getCategory());
        assertEquals(88L, existingExpense.getCategory().getId());
    }

    @Test
    void toEntity_WithEmptyDescription_MapsCorrectly() {
        // Given
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("25.00"),
                LocalDate.of(2024, 12, 3),
                "",
                1
        );

        // When
        Expense expense = expenseMapper.toEntity(request);

        // Then
        assertNotNull(expense);
        assertEquals(new BigDecimal("25.00"), expense.getAmount());
        assertEquals(LocalDate.of(2024, 12, 3), expense.getDate());
        assertEquals("", expense.getDescription());
    }

    @Test
    void toResponse_WithAllFields_MapsCorrectly() {
        // Given
        Category category = new Category();
        category.setId(10L);
        category.setName("Entertainment");

        Expense expense = new Expense();
        expense.setId(42L);
        expense.setAmount(new BigDecimal("120.50"));
        expense.setDate(LocalDate.of(2024, 6, 15));
        expense.setDescription("Concert tickets");
        expense.setUser(testUser);
        expense.setCategory(category);
        expense.setCreatedAt(LocalDateTime.of(2024, 6, 15, 9, 30));
        expense.setUpdatedAt(LocalDateTime.of(2024, 6, 20, 14, 45));

        // When
        ExpenseResponse response = expenseMapper.toResponse(expense);

        // Then
        assertEquals(42L, response.id());
        assertEquals(new BigDecimal("120.50"), response.amount());
        assertEquals(LocalDate.of(2024, 6, 15), response.date());
        assertEquals("Concert tickets", response.description());
        assertEquals(10, response.categoryId());
    }

    @Test
    void toEntity_WithDecimalAmount_PreservesScale() {
        // Given
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("99.99"),
                LocalDate.of(2024, 12, 1),
                "Test expense",
                1
        );

        // When
        Expense expense = expenseMapper.toEntity(request);

        // Then
        assertEquals(new BigDecimal("99.99"), expense.getAmount());
    }

    @Test
    void toResponse_WithDecimalAmount_PreservesScale() {
        // Given
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setAmount(new BigDecimal("123.45"));
        expense.setDate(LocalDate.of(2024, 12, 1));
        expense.setDescription("Test");
        expense.setUser(testUser);
        expense.setCategory(testCategory);

        // When
        ExpenseResponse response = expenseMapper.toResponse(expense);

        // Then
        assertEquals(new BigDecimal("123.45"), response.amount());
    }
}
