package com.expensetracker.service;

import com.expensetracker.dto.auth.AuthResponse;
import com.expensetracker.dto.auth.LoginRequest;
import com.expensetracker.dto.auth.RegisterRequest;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "test@example.com",
                "testuser",
                "password123",
                "Test",
                "User"
        );

        loginRequest = new LoginRequest("testuser", "password123");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEnabled(true);
    }

    @Test
    void register_Success() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.username())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("accessToken123");
        when(tokenProvider.generateRefreshToken(authentication)).thenReturn("refreshToken123");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken123");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken123");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        verify(userRepository).existsByEmail(registerRequest.email());
        verify(userRepository).existsByUsername(registerRequest.username());
        verify(passwordEncoder).encode(registerRequest.password());
        verify(userRepository).save(any(User.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
        verify(tokenProvider).generateRefreshToken(authentication);
    }

    @Test
    void register_EmailAlreadyExists_ThrowsBadRequestException() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email already exists");

        verify(userRepository).existsByEmail(registerRequest.email());
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_UsernameAlreadyExists_ThrowsBadRequestException() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.username())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Username already exists");

        verify(userRepository).existsByEmail(registerRequest.email());
        verify(userRepository).existsByUsername(registerRequest.username());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("accessToken123");
        when(tokenProvider.generateRefreshToken(authentication)).thenReturn("refreshToken123");
        when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.of(user));

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken123");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken123");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
        verify(tokenProvider).generateRefreshToken(authentication);
        verify(userRepository).findByUsername(loginRequest.username());
    }

    @Test
    void login_UserNotFound_ThrowsBadRequestException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("accessToken123");
        when(tokenProvider.generateRefreshToken(authentication)).thenReturn("refreshToken123");
        when(userRepository.findByUsername(loginRequest.username())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername(loginRequest.username());
    }
}
