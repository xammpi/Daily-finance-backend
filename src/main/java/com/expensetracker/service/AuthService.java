package com.expensetracker.service;

import com.expensetracker.dto.auth.AuthResponse;
import com.expensetracker.dto.auth.LoginRequest;
import com.expensetracker.dto.auth.RegisterRequest;
import com.expensetracker.entity.Currency;
import com.expensetracker.entity.User;
import com.expensetracker.entity.Wallet;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.repository.CurrencyRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new BadRequestException("Username already exists");
        }

        if (request.currencyId() == null) {
            throw new BadRequestException("Currency id is required");
        }
        Currency currency = currencyRepository.findById(request.currencyId().longValue())
                .orElseThrow(() -> new BadRequestException("Currency id not found"));

        // Use constructor - entity manages its own state
        User user = new User(
                request.email(),
                request.username(),
                passwordEncoder.encode(request.password()),
                request.firstName(),
                request.lastName()
        );

        // Use constructor - entity manages its own state
        Wallet wallet = new Wallet(user, currency);
        user.addWallet(wallet);

        // Save user with cascade to automatically save wallet
        user = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getUsername());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BadRequestException("User not found"));

        return new AuthResponse(accessToken, refreshToken, user.getId(), user.getUsername());
    }
}
