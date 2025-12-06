package com.expensetracker.repository;

import com.expensetracker.entity.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {

    List<Deposit> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Deposit d WHERE d.user.id = :userId")
    BigDecimal getTotalDepositsByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Deposit d WHERE d.user.id = :userId AND d.date BETWEEN :startDate AND :endDate")
    BigDecimal getTotalDepositsByUserIdAndDateBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

}
