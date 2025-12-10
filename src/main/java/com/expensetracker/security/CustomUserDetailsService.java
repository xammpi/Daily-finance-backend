package com.expensetracker.security;

import com.expensetracker.entity.User;
import com.expensetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'username:' + #username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Use optimized query that fetches user with wallet and currency in single query
        User user = userRepository.findByUsernameWithWallet(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'id:' + #id")
    public UserDetails loadUserById(Long id) {
        // Use optimized query that fetches user with wallet and currency in single query
        // This method is called on EVERY authenticated request via JwtAuthenticationFilter
        User user = userRepository.findByIdWithWallet(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return UserPrincipal.create(user);
    }
}
