package com.expensetracker.security;

import com.expensetracker.entity.Currency;
import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEnabled(true);
        testUser.setBalance(BigDecimal.ZERO);
        testUser.setCurrency(Currency.USD);
    }

    @Test
    void loadUserByUsername_WithValidUsername_ReturnsUserDetails() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertInstanceOf(UserPrincipal.class, userDetails);
    }

    @Test
    void loadUserByUsername_WithInvalidUsername_ThrowsException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    void loadUserByUsername_ReturnsUserPrincipalWithCorrectDetails() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;

        // Then
        assertEquals(1L, userPrincipal.getId());
        assertEquals("test@example.com", userPrincipal.getEmail());
        assertEquals("testuser", userPrincipal.getUsername());
        assertEquals("encodedPassword", userPrincipal.getPassword());
        assertTrue(userPrincipal.isEnabled());
    }

    @Test
    void loadUserById_WithValidId_ReturnsUserDetails() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserById(1L);

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertInstanceOf(UserPrincipal.class, userDetails);
    }

    @Test
    void loadUserById_WithInvalidId_ThrowsException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserById(999L);
        });
    }

    @Test
    void loadUserById_ReturnsUserPrincipalWithCorrectDetails() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserById(1L);
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;

        // Then
        assertEquals(1L, userPrincipal.getId());
        assertEquals("test@example.com", userPrincipal.getEmail());
        assertEquals("testuser", userPrincipal.getUsername());
        assertEquals("encodedPassword", userPrincipal.getPassword());
        assertTrue(userPrincipal.isEnabled());
    }

    @Test
    void loadUserByUsername_WithDisabledUser_ReturnsDisabledUserDetails() {
        // Given
        testUser.setEnabled(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void loadUserById_WithDisabledUser_ReturnsDisabledUserDetails() {
        // Given
        testUser.setEnabled(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserById(1L);

        // Then
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
    }

    @Test
    void loadUserByUsername_ChecksAuthorities() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails.getAuthorities());
        assertFalse(userDetails.getAuthorities().isEmpty());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }
}
