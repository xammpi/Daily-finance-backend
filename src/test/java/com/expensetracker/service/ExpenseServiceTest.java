package com.expensetracker.service;

import com.expensetracker.dto.expense.ExpenseRequest;
import com.expensetracker.dto.expense.ExpenseResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.ExpenseMapper;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private ExpenseService expenseService;

    private User user;
    private Category category;
    private Expense expense;
    private ExpenseRequest expenseRequest;
    private ExpenseResponse expenseResponse;
    private final Long userId = 1L;
    private final Long categoryId = 1L;
    private final Long expenseId = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setBalance(new BigDecimal("1000.00"));

        category = new Category();
        category.setId(categoryId);
        category.setName("Groceries");
        category.setUser(user);

        expense = new Expense();
        expense.setId(expenseId);
        expense.setAmount(new BigDecimal("100.00"));
        expense.setDate(LocalDate.now());
        expense.setDescription("Weekly shopping");
        expense.setUser(user);
        expense.setCategory(category);

        expenseRequest = new ExpenseRequest(
                new BigDecimal("100.00"),
                LocalDate.now(),
                "Weekly shopping",
                1
        );

        expenseResponse = new ExpenseResponse(
                expenseId,
                new BigDecimal("100.00"),
                LocalDate.now(),
                "Weekly shopping",
                1
        );

        // Mock security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(userId);
    }

    @Test
    void createExpense_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(expenseMapper.toEntity(expenseRequest)).thenReturn(expense);
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(expenseMapper.toResponse(expense)).thenReturn(expenseResponse);

        // Act
        ExpenseResponse response = expenseService.createExpense(expenseRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(expenseId);
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(response.description()).isEqualTo("Weekly shopping");
        assertThat(response.categoryId()).isEqualTo(1);

        verify(userRepository).findById(userId);
        verify(categoryRepository).findById(categoryId);
        verify(expenseMapper).toEntity(expenseRequest);
        verify(expenseRepository).save(any(Expense.class));
        verify(userRepository).save(any(User.class));
        verify(expenseMapper).toResponse(expense);
    }

    @Test
    void createExpense_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> expenseService.createExpense(expenseRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createExpense_CategoryNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> expenseService.createExpense(expenseRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");

        verify(userRepository).findById(userId);
        verify(categoryRepository).findById(categoryId);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void createExpense_CategoryDoesNotBelongToUser_ThrowsBadRequestException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        category.setUser(otherUser);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // Act & Assert
        assertThatThrownBy(() -> expenseService.createExpense(expenseRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Category does not belong to current user");

        verify(userRepository).findById(userId);
        verify(categoryRepository).findById(categoryId);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void updateExpense_Success() {
        // Arrange
        ExpenseRequest updateRequest = new ExpenseRequest(
                new BigDecimal("150.00"),
                LocalDate.now(),
                "Updated shopping",
                1
        );

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        doNothing().when(expenseMapper).updateEntityFromRequest(updateRequest, expense);
        when(expenseRepository.save(expense)).thenReturn(expense);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(expenseMapper.toResponse(expense)).thenReturn(expenseResponse);

        // Act
        ExpenseResponse response = expenseService.updateExpense(expenseId, updateRequest);

        // Assert
        assertThat(response).isNotNull();

        verify(expenseRepository).findById(expenseId);
        verify(categoryRepository).findById(categoryId);
        verify(expenseMapper).updateEntityFromRequest(updateRequest, expense);
        verify(expenseRepository).save(expense);
        verify(userRepository).save(any(User.class));
        verify(expenseMapper).toResponse(expense);
    }

    @Test
    void updateExpense_ExpenseNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        ExpenseRequest updateRequest = new ExpenseRequest(
                new BigDecimal("150.00"),
                LocalDate.now(),
                "Updated shopping",
                1
        );

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> expenseService.updateExpense(expenseId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Expense not found");

        verify(expenseRepository).findById(expenseId);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void updateExpense_ExpenseDoesNotBelongToUser_ThrowsBadRequestException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        expense.setUser(otherUser);

        ExpenseRequest updateRequest = new ExpenseRequest(
                new BigDecimal("150.00"),
                LocalDate.now(),
                "Updated shopping",
                1
        );

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        // Act & Assert
        assertThatThrownBy(() -> expenseService.updateExpense(expenseId, updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Expense does not belong to current user");

        verify(expenseRepository).findById(expenseId);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void updateExpense_CategoryNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        ExpenseRequest updateRequest = new ExpenseRequest(
                new BigDecimal("150.00"),
                LocalDate.now(),
                "Updated shopping",
                1
        );

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        doNothing().when(expenseMapper).updateEntityFromRequest(updateRequest, expense);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> expenseService.updateExpense(expenseId, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");

        verify(expenseRepository).findById(expenseId);
        verify(categoryRepository).findById(categoryId);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void updateExpense_CategoryDoesNotBelongToUser_ThrowsBadRequestException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        Category otherCategory = new Category();
        otherCategory.setId(categoryId);
        otherCategory.setUser(otherUser);

        ExpenseRequest updateRequest = new ExpenseRequest(
                new BigDecimal("150.00"),
                LocalDate.now(),
                "Updated shopping",
                1
        );

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        doNothing().when(expenseMapper).updateEntityFromRequest(updateRequest, expense);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(otherCategory));

        // Act & Assert
        assertThatThrownBy(() -> expenseService.updateExpense(expenseId, updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Category does not belong to current user");

        verify(expenseRepository).findById(expenseId);
        verify(categoryRepository).findById(categoryId);
        verify(expenseRepository, never()).save(any(Expense.class));
    }

    @Test
    void getUserExpenses_Success() {
        // Arrange
        Expense expense2 = new Expense();
        expense2.setId(2L);
        expense2.setAmount(new BigDecimal("200.00"));
        expense2.setDate(LocalDate.now());
        expense2.setDescription("Another expense");
        expense2.setUser(user);
        expense2.setCategory(category);

        List<Expense> expenses = Arrays.asList(expense, expense2);

        ExpenseResponse expenseResponse2 = new ExpenseResponse(
                2L,
                new BigDecimal("200.00"),
                LocalDate.now(),
                "Another expense",
                1
        );

        when(expenseRepository.findByUserId(userId)).thenReturn(expenses);
        when(expenseMapper.toResponse(expense)).thenReturn(expenseResponse);
        when(expenseMapper.toResponse(expense2)).thenReturn(expenseResponse2);

        // Act
        List<ExpenseResponse> responses = expenseService.getUserExpenses();

        // Assert
        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).description()).isEqualTo("Weekly shopping");
        assertThat(responses.get(1).description()).isEqualTo("Another expense");

        verify(expenseRepository).findByUserId(userId);
        verify(expenseMapper, times(2)).toResponse(any(Expense.class));
    }

    @Test
    void getUserExpenses_EmptyList_Success() {
        // Arrange
        when(expenseRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<ExpenseResponse> responses = expenseService.getUserExpenses();

        // Assert
        assertThat(responses).isNotNull();
        assertThat(responses).isEmpty();

        verify(expenseRepository).findByUserId(userId);
        verify(expenseMapper, never()).toResponse(any(Expense.class));
    }

    @Test
    void getExpenseById_Success() {
        // Arrange
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(expenseMapper.toResponse(expense)).thenReturn(expenseResponse);

        // Act
        ExpenseResponse response = expenseService.getExpenseById(expenseId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(expenseId);
        assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("100.00"));

        verify(expenseRepository).findById(expenseId);
        verify(expenseMapper).toResponse(expense);
    }

    @Test
    void getExpenseById_ExpenseNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> expenseService.getExpenseById(expenseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Expense not found");

        verify(expenseRepository).findById(expenseId);
        verify(expenseMapper, never()).toResponse(any(Expense.class));
    }

    @Test
    void getExpenseById_ExpenseDoesNotBelongToUser_ThrowsBadRequestException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        expense.setUser(otherUser);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        // Act & Assert
        assertThatThrownBy(() -> expenseService.getExpenseById(expenseId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Expense does not belong to current user");

        verify(expenseRepository).findById(expenseId);
        verify(expenseMapper, never()).toResponse(any(Expense.class));
    }

    @Test
    void deleteExpense_Success() {
        // Arrange
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(expenseRepository).delete(expense);

        // Act
        expenseService.deleteExpense(expenseId);

        // Assert
        verify(expenseRepository).findById(expenseId);
        verify(userRepository).save(any(User.class));
        verify(expenseRepository).delete(expense);
    }

    @Test
    void deleteExpense_ExpenseNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> expenseService.deleteExpense(expenseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Expense not found");

        verify(expenseRepository).findById(expenseId);
        verify(expenseRepository, never()).delete(any(Expense.class));
    }

    @Test
    void deleteExpense_ExpenseDoesNotBelongToUser_ThrowsBadRequestException() {
        // Arrange
        User otherUser = new User();
        otherUser.setId(2L);
        expense.setUser(otherUser);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        // Act & Assert
        assertThatThrownBy(() -> expenseService.deleteExpense(expenseId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Expense does not belong to current user");

        verify(expenseRepository).findById(expenseId);
        verify(expenseRepository, never()).delete(any(Expense.class));
    }
}
