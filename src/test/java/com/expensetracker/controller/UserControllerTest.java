package com.expensetracker.controller;

import com.expensetracker.dto.user.BalanceSummaryResponse;
import com.expensetracker.dto.user.DepositRequest;
import com.expensetracker.dto.user.UserProfileResponse;
import com.expensetracker.entity.Currency;
import com.expensetracker.security.JwtAuthenticationFilter;
import com.expensetracker.security.JwtTokenProvider;
import com.expensetracker.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void getUserProfile_ReturnsUserProfile() throws Exception {
        // Given
        UserProfileResponse response = new UserProfileResponse(
                1L,
                "testuser",
                "test@example.com",
                "Test",
                "User",
                new BigDecimal("1000.00"),
                Currency.USD
        );

        when(userService.getUserProfile()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void depositMoney_WithValidRequest_ReturnsUpdatedProfile() throws Exception {
        // Given
        DepositRequest request = new DepositRequest(new BigDecimal("500.00"));

        UserProfileResponse response = new UserProfileResponse(
                1L,
                "testuser",
                "test@example.com",
                "Test",
                "User",
                new BigDecimal("1500.00"),
                Currency.USD
        );

        when(userService.depositMoney(any(DepositRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/user/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500.00));
    }

    @Test
    void depositMoney_WithNullAmount_ReturnsBadRequest() throws Exception {
        // Given
        String requestJson = "{\"amount\": null}";

        // When & Then
        mockMvc.perform(post("/api/v1/user/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCurrency_WithValidCurrency_ReturnsUpdatedProfile() throws Exception {
        // Given
        UserProfileResponse response = new UserProfileResponse(
                1L,
                "testuser",
                "test@example.com",
                "Test",
                "User",
                new BigDecimal("1000.00"),
                Currency.EUR
        );

        when(userService.updateCurrency(Currency.EUR)).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/user/currency")
                        .param("currency", "EUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    void updateCurrency_WithInvalidCurrency_ReturnsBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(put("/api/v1/user/currency")
                        .param("currency", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBalanceSummary_ReturnsBalanceSummary() throws Exception {
        // Given
        BalanceSummaryResponse response = new BalanceSummaryResponse(
                new BigDecimal("1000.00"),
                new BigDecimal("250.00"),
                new BigDecimal("1000.00"),
                Currency.USD
        );

        when(userService.getBalanceSummary()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/user/balance-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentBalance").value(1000.00))
                .andExpect(jsonPath("$.totalExpensesThisMonth").value(250.00))
                .andExpect(jsonPath("$.remainingBalance").value(1000.00))
                .andExpect(jsonPath("$.currency").value("USD"));
    }
}
