package com.expensetracker.service;


import com.expensetracker.dto.expense.ExpenseRequest;
import com.expensetracker.dto.expense.ExpenseResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Expense;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.ExpenseMapper;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.ExpenseRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;

    private Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById((request.categoryId().longValue()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(userId)) {
            throw new BadRequestException("Category does not belong to current user");
        }

        // Deduct expense amount from user balance
        user.setBalance(user.getBalance().subtract(request.amount()));

        Expense expense = expenseMapper.toEntity(request);
        expense.setUser(user);
        expense.setCategory(category);

        expense = expenseRepository.save(expense);
        userRepository.save(user);

        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Long userId = getCurrentUserId();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getId().equals(userId)) {
            throw new BadRequestException("Expense does not belong to current user");
        }

        User user = expense.getUser();

        // Reverse the old expense amount (add it back)
        user.setBalance(user.getBalance().add(expense.getAmount()));

        // Update expense fields
        expenseMapper.updateEntityFromRequest(request, expense);

        Category category = categoryRepository.findById((request.categoryId().longValue()))
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(userId)) {
            throw new BadRequestException("Category does not belong to current user");
        }

        expense.setCategory(category);

        // Deduct the new expense amount
        user.setBalance(user.getBalance().subtract(expense.getAmount()));

        expense = expenseRepository.save(expense);
        userRepository.save(user);

        return expenseMapper.toResponse(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getUserExpenses() {
        Long userId = getCurrentUserId();
        return expenseRepository.findByUserId(userId).stream()
                .map(expenseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long id) {
        Long userId = getCurrentUserId();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getId().equals(userId)) {
            throw new BadRequestException("Expense does not belong to current user");
        }

        return expenseMapper.toResponse(expense);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Long userId = getCurrentUserId();
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getUser().getId().equals(userId)) {
            throw new BadRequestException("Expense does not belong to current user");
        }

        User user = expense.getUser();

        // Add the expense amount back to balance when deleting
        user.setBalance(user.getBalance().add(expense.getAmount()));

        userRepository.save(user);
        expenseRepository.delete(expense);
    }

}
