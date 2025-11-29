package com.expensetracker.repository;

import com.expensetracker.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findByUserId(Long userId);

    List<RecurringTransaction> findByUserIdAndActiveTrue(Long userId);

    @Query("SELECT rt FROM RecurringTransaction rt WHERE rt.active = true AND rt.nextOccurrence <= :date")
    List<RecurringTransaction> findDueRecurringTransactions(LocalDate date);
}
