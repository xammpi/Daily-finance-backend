package com.expensetracker.controller;

import com.expensetracker.dto.auth.AuthResponse;
import com.expensetracker.dto.auth.LoginRequest;
import com.expensetracker.dto.auth.RegisterRequest;
import com.expensetracker.security.JwtAuthenticationFilter;
import com.expensetracker.security.JwtTokenProvider;
import com.expensetracker.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void register_WithValidRequest_ReturnsAuthResponse() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "testuser",
                "password123",
                "Test",
                "User"
        );

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("access-token");
        authResponse.setRefreshToken("refresh-token");
        authResponse.setTokenType("Bearer");
        authResponse.setUserId(1L);
        authResponse.setUsername("testuser");

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void register_WithInvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "invalid-email",
                "testuser",
                "password123",
                "Test",
                "User"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithBlankUsername_ReturnsBadRequest() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "",
                "password123",
                "Test",
                "User"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithShortUsername_ReturnsBadRequest() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "ab",
                "password123",
                "Test",
                "User"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithShortPassword_ReturnsBadRequest() throws Exception {
        // Given
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "testuser",
                "12345",
                "Test",
                "User"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithValidRequest_ReturnsAuthResponse() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken("access-token");
        authResponse.setRefreshToken("refresh-token");
        authResponse.setTokenType("Bearer");
        authResponse.setUserId(1L);
        authResponse.setUsername("testuser");

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void login_WithBlankUsername_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("", "password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_WithBlankPassword_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
