package com.expensetracker.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String jwtSecret;
    private long jwtExpiration;
    private long jwtRefreshExpiration;
    private SecretKey secretKey;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        jwtSecret = "mySecretKeyForJWTTokenGenerationAndValidation1234567890";
        jwtExpiration = 86400000L; // 24 hours in milliseconds
        jwtRefreshExpiration = 604800000L; // 7 days in milliseconds

        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", jwtExpiration);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtRefreshExpiration", jwtRefreshExpiration);

        // Initialize the key
        secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtTokenProvider, "key", secretKey);
    }

    @Test
    void generateToken_WithValidAuthentication_ReturnsToken() {
        // Given
        UserPrincipal userPrincipal = new UserPrincipal(
                1L,
                "test@example.com",
                "testuser",
                "password",
                true
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // When
        String token = jwtTokenProvider.generateToken(authentication);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void generateRefreshToken_WithValidAuthentication_ReturnsToken() {
        // Given
        UserPrincipal userPrincipal = new UserPrincipal(
                1L,
                "test@example.com",
                "testuser",
                "password",
                true
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // When
        String token = jwtTokenProvider.generateRefreshToken(authentication);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getUserIdFromToken_WithValidToken_ReturnsUserId() {
        // Given
        Long userId = 1L;
        UserPrincipal userPrincipal = new UserPrincipal(
                userId,
                "test@example.com",
                "testuser",
                "password",
                true
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        String token = jwtTokenProvider.generateToken(authentication);

        // When
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void validateToken_WithValidToken_ReturnsTrue() {
        // Given
        UserPrincipal userPrincipal = new UserPrincipal(
                1L,
                "test@example.com",
                "testuser",
                "password",
                true
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        String token = jwtTokenProvider.generateToken(authentication);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_WithInvalidToken_ReturnsFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithExpiredToken_ReturnsFalse() {
        // Given
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() - 1000); // Expired 1 second ago

        String expiredToken = Jwts.builder()
                .subject("1")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();

        // When
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithMalformedToken_ReturnsFalse() {
        // Given
        String malformedToken = "malformed-token";

        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void validateToken_WithEmptyToken_ReturnsFalse() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void generateToken_TokenContainsCorrectSubject() {
        // Given
        Long userId = 123L;
        UserPrincipal userPrincipal = new UserPrincipal(
                userId,
                "test@example.com",
                "testuser",
                "password",
                true
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // When
        String token = jwtTokenProvider.generateToken(authentication);
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void generateToken_AndRefreshToken_BothAreValid() {
        // Given
        UserPrincipal userPrincipal = new UserPrincipal(
                1L,
                "test@example.com",
                "testuser",
                "password",
                true
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // When
        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // Then
        assertTrue(jwtTokenProvider.validateToken(accessToken));
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
        assertNotEquals(accessToken, refreshToken);
    }
}
