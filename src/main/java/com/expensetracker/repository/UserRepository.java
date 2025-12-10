package com.expensetracker.repository;

import com.expensetracker.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    /**
     * Find user by ID with wallet eagerly loaded to avoid N+1 queries
     */
    @EntityGraph(attributePaths = {"wallet", "wallet.currency"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithWallet(@Param("id") Long id);

    /**
     * Find user by username with wallet eagerly loaded to avoid N+1 queries
     * Used in authentication flows where user details are needed
     */
    @EntityGraph(attributePaths = {"wallet", "wallet.currency"})
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameWithWallet(@Param("username") String username);
}
