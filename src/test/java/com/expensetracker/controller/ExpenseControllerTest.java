package com.expensetracker.controller;

import com.expensetracker.dto.expense.ExpenseRequest;
import com.expensetracker.dto.expense.ExpenseResponse;
import com.expensetracker.security.JwtAuthenticationFilter;
import com.expensetracker.security.JwtTokenProvider;
import com.expensetracker.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void createExpense_WithValidRequest_ReturnsCreatedExpense() throws Exception {
        // Given
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("50.00"),
                LocalDate.of(2024, 12, 1),
                "Grocery shopping",
                1
        );

        ExpenseResponse response = new ExpenseResponse(
                1L,
                new BigDecimal("50.00"),
                LocalDate.of(2024, 12, 1),
                "Grocery shopping",
                1
        );

        when(expenseService.createExpense(any(ExpenseRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.date").value("2024-12-01"))
                .andExpect(jsonPath("$.description").value("Grocery shopping"))
                .andExpect(jsonPath("$.categoryId").value(1));
    }

    @Test
    void createExpense_WithNullAmount_ReturnsBadRequest() throws Exception {
        // Given
        String requestJson = "{\"amount\": null, \"date\": \"2024-12-01\", \"description\": \"Test\", \"categoryId\": 1}";

        // When & Then
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createExpense_WithNullDate_ReturnsBadRequest() throws Exception {
        // Given
        String requestJson = "{\"amount\": 50.00, \"date\": null, \"description\": \"Test\", \"categoryId\": 1}";

        // When & Then
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createExpense_WithNullCategoryId_ReturnsBadRequest() throws Exception {
        // Given
        String requestJson = "{\"amount\": 50.00, \"date\": \"2024-12-01\", \"description\": \"Test\", \"categoryId\": null}";

        // When & Then
        mockMvc.perform(post("/api/v1/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserExpenses_ReturnsListOfExpenses() throws Exception {
        // Given
        List<ExpenseResponse> expenses = Arrays.asList(
                new ExpenseResponse(1L, new BigDecimal("50.00"), LocalDate.of(2024, 12, 1), "Grocery", 1),
                new ExpenseResponse(2L, new BigDecimal("30.00"), LocalDate.of(2024, 12, 2), "Transport", 2)
        );

        when(expenseService.getUserExpenses()).thenReturn(expenses);

        // When & Then
        mockMvc.perform(get("/api/v1/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].amount").value(50.00))
                .andExpect(jsonPath("$[0].description").value("Grocery"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].amount").value(30.00))
                .andExpect(jsonPath("$[1].description").value("Transport"));
    }

    @Test
    void getExpenseById_WithValidId_ReturnsExpense() throws Exception {
        // Given
        ExpenseResponse response = new ExpenseResponse(
                1L,
                new BigDecimal("50.00"),
                LocalDate.of(2024, 12, 1),
                "Grocery shopping",
                1
        );

        when(expenseService.getExpenseById(1L)).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/expenses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(50.00))
                .andExpect(jsonPath("$.description").value("Grocery shopping"))
                .andExpect(jsonPath("$.categoryId").value(1));
    }

    @Test
    void updateExpense_WithValidRequest_ReturnsUpdatedExpense() throws Exception {
        // Given
        ExpenseRequest request = new ExpenseRequest(
                new BigDecimal("75.00"),
                LocalDate.of(2024, 12, 1),
                "Updated grocery shopping",
                1
        );

        ExpenseResponse response = new ExpenseResponse(
                1L,
                new BigDecimal("75.00"),
                LocalDate.of(2024, 12, 1),
                "Updated grocery shopping",
                1
        );

        when(expenseService.updateExpense(eq(1L), any(ExpenseRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(put("/api/v1/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(75.00))
                .andExpect(jsonPath("$.description").value("Updated grocery shopping"));
    }

    @Test
    void updateExpense_WithNullAmount_ReturnsBadRequest() throws Exception {
        // Given
        String requestJson = "{\"amount\": null, \"date\": \"2024-12-01\", \"description\": \"Test\", \"categoryId\": 1}";

        // When & Then
        mockMvc.perform(put("/api/v1/expenses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteExpense_WithValidId_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(expenseService).deleteExpense(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/expenses/1"))
                .andExpect(status().isNoContent());

        verify(expenseService, times(1)).deleteExpense(1L);
    }
}
