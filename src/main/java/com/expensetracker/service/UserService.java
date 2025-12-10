package com.expensetracker.service;

import com.expensetracker.dto.user.UserProfileResponse;
import com.expensetracker.dto.user.WalletResponse;
import com.expensetracker.entity.Currency;
import com.expensetracker.entity.User;
import com.expensetracker.entity.Wallet;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.CurrencyMapper;
import com.expensetracker.repository.CurrencyRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.repository.WalletRepository;
import com.expensetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;

    private Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile() {
        Long userId = getCurrentUserId();
        // Use optimized query that fetches user with wallet in single query
        // Note: User authentication is already cached via CustomUserDetailsService
        User user = userRepository.findByIdWithWallet(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                wallet.getCurrency().getId()
        );
    }

    @Transactional
    public UserProfileResponse updateCurrency(Long currencyId) {
        Long userId = getCurrentUserId();
        User user = userRepository.findByIdWithWallet(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        Currency currency = currencyRepository.findById(currencyId)
                .orElseThrow(() -> new BadRequestException("Currency not found"));

        // Use rich domain model - wallet handles currency change
        wallet.changeCurrency(currency);
        walletRepository.save(wallet);

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                wallet.getCurrency().getId()
        );
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletDetails() {
        Long userId = getCurrentUserId();
        User user = userRepository.findByIdWithWallet(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = user.getWallet();
        if (wallet == null) {
            throw new ResourceNotFoundException("Wallet not found for user");
        }

        return new WalletResponse(
                wallet.getId(),
                wallet.getAmount(),
                currencyMapper.toResponse(wallet.getCurrency())
        );
    }

}
