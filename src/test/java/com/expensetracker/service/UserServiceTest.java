package com.expensetracker.service;

import com.expensetracker.dto.user.BalanceSummaryResponse;
import com.expensetracker.dto.user.DepositRequest;
import com.expensetracker.dto.user.UserProfileResponse;
import com.expensetracker.entity.Currency;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.exception.ResourceNotFoundException;
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
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private UserService userService;

    private User user;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setBalance(new BigDecimal("1000.00"));
        user.setCurrency(Currency.USD);

        // Mock security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(userId);
    }

    @Test
    void getUserProfile_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserProfileResponse response = userService.getUserProfile();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.firstName()).isEqualTo("Test");
        assertThat(response.lastName()).isEqualTo("User");
        assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.currency()).isEqualTo(Currency.USD);

        verify(userRepository).findById(userId);
    }

    @Test
    void getUserProfile_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserProfile())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
    }

    @Test
    void depositMoney_Success() {
        // Arrange
        DepositRequest depositRequest = new DepositRequest(new BigDecimal("500.00"));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("test@example.com");
        updatedUser.setFirstName("Test");
        updatedUser.setLastName("User");
        updatedUser.setBalance(new BigDecimal("1500.00"));
        updatedUser.setCurrency(Currency.USD);

        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserProfileResponse response = userService.depositMoney(depositRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("1500.00"));

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void depositMoney_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        DepositRequest depositRequest = new DepositRequest(new BigDecimal("500.00"));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.depositMoney(depositRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateCurrency_Success() {
        // Arrange
        Currency newCurrency = Currency.EUR;
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername("testuser");
        updatedUser.setEmail("test@example.com");
        updatedUser.setFirstName("Test");
        updatedUser.setLastName("User");
        updatedUser.setBalance(new BigDecimal("1000.00"));
        updatedUser.setCurrency(Currency.EUR);

        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserProfileResponse response = userService.updateCurrency(newCurrency);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.currency()).isEqualTo(Currency.EUR);

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateCurrency_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        Currency newCurrency = Currency.EUR;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateCurrency(newCurrency))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getBalanceSummary_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        Expense expense1 = new Expense();
        expense1.setAmount(new BigDecimal("100.00"));

        Expense expense2 = new Expense();
        expense2.setAmount(new BigDecimal("200.00"));

        List<Expense> expenses = Arrays.asList(expense1, expense2);
        when(expenseRepository.findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth))
                .thenReturn(expenses);

        // Act
        BalanceSummaryResponse response = userService.getBalanceSummary();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.currentBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.totalExpensesThisMonth()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(response.remainingBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.currency()).isEqualTo(Currency.USD);

        verify(userRepository).findById(userId);
        verify(expenseRepository).findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);
    }

    @Test
    void getBalanceSummary_NoExpenses_Success() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        when(expenseRepository.findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth))
                .thenReturn(Collections.emptyList());

        // Act
        BalanceSummaryResponse response = userService.getBalanceSummary();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.currentBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.totalExpensesThisMonth()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.remainingBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(response.currency()).isEqualTo(Currency.USD);

        verify(userRepository).findById(userId);
        verify(expenseRepository).findByUserIdAndDateBetween(userId, startOfMonth, endOfMonth);
    }

    @Test
    void getBalanceSummary_UserNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getBalanceSummary())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findById(userId);
        verify(expenseRepository, never()).findByUserIdAndDateBetween(any(), any(), any());
    }
}
