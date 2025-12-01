package com.expensetracker.service;

import com.expensetracker.dto.transaction.TransactionRequest;
import com.expensetracker.dto.transaction.TransactionResponse;
import com.expensetracker.entity.Category;
import com.expensetracker.entity.Transaction;
import com.expensetracker.entity.User;
import com.expensetracker.exception.BadRequestException;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.TransactionMapper;
import com.expensetracker.repository.CategoryRepository;
import com.expensetracker.repository.TransactionRepository;
import com.expensetracker.repository.UserRepository;
import com.expensetracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;

    private Long getCurrentUserId() {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userPrincipal.getId();
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Long userId = getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getUser().getId().equals(userId)) {
            throw new BadRequestException("Category does not belong to current user");
        }

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setUser(user);
        transaction.setCategory(category);

        updateUserBalance(user, transaction.getAmount(), transaction.getType());

        transaction = transactionRepository.save(transaction);
        userRepository.save(user);

        return transactionMapper.toResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(Pageable pageable) {
        Long userId = getCurrentUserId();
        return transactionRepository.findByUserId(userId, pageable)
                .map(transactionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        Long userId = getCurrentUserId();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new BadRequestException("Transaction does not belong to current user");
        }

        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        Long userId = getCurrentUserId();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new BadRequestException("Transaction does not belong to current user");
        }

        User user = transaction.getUser();
        BigDecimal oldAmount = transaction.getAmount();
        Transaction.TransactionType oldType = transaction.getType();

        reverseUserBalance(user, oldAmount, oldType);

        if (!request.getCategoryId().equals(transaction.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

            if (!newCategory.getUser().getId().equals(userId)) {
                throw new BadRequestException("Category does not belong to current user");
            }
            transaction.setCategory(newCategory);
        }

        transactionMapper.updateEntityFromRequest(request, transaction);
        updateUserBalance(user, transaction.getAmount(), transaction.getType());

        transaction = transactionRepository.save(transaction);
        userRepository.save(user);
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Long userId = getCurrentUserId();
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(userId)) {
            throw new BadRequestException("Transaction does not belong to current user");
        }

        User user = transaction.getUser();
        reverseUserBalance(user, transaction.getAmount(), transaction.getType());
        userRepository.save(user);
        transactionRepository.delete(transaction);
    }

    private void updateUserBalance(User user, BigDecimal amount, Transaction.TransactionType type) {
        if (type == Transaction.TransactionType.INCOME) {
            user.setBalance(user.getBalance().add(amount));
        } else {
            user.setBalance(user.getBalance().subtract(amount));
        }
    }

    private void reverseUserBalance(User user, BigDecimal amount, Transaction.TransactionType type) {
        if (type == Transaction.TransactionType.INCOME) {
            user.setBalance(user.getBalance().subtract(amount));
        } else {
            user.setBalance(user.getBalance().add(amount));
        }
    }
}
