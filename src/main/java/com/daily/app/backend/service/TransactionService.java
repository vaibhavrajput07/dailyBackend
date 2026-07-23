package com.daily.app.backend.service;

import com.daily.app.backend.dto.transaction.AddExpenseRequest;
import com.daily.app.backend.dto.transaction.AddSaleRequest;
import com.daily.app.backend.dto.transaction.TransactionResponse;
import com.daily.app.backend.dto.transaction.UpdateTransactionRequest;
import com.daily.app.backend.entity.Business;
import com.daily.app.backend.entity.ExpenseCategory;
import com.daily.app.backend.entity.Transaction;
import com.daily.app.backend.entity.TransactionType;
import com.daily.app.backend.exception.ResourceNotFoundException;
import com.daily.app.backend.mapper.TransactionMapper;
import com.daily.app.backend.repository.ExpenseCategoryRepository;
import com.daily.app.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository,
                               ExpenseCategoryRepository expenseCategoryRepository,
                               TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
        this.transactionMapper = transactionMapper;
    }

    @Transactional
    public TransactionResponse addSale(Long businessId, AddSaleRequest request) {
        Transaction transaction = Transaction.builder()
                .business(businessRef(businessId))
                .type(TransactionType.SALE)
                .amount(request.amount())
                .note(request.note())
                .paymentMethod(request.paymentMethod())
                .transactionDate(request.transactionDate() != null ? request.transactionDate() : Instant.now())
                .build();

        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse addExpense(Long businessId, AddExpenseRequest request) {
        ExpenseCategory category = null;
        if (request.categoryId() != null) {
            category = expenseCategoryRepository.findByIdAndBusinessId(request.categoryId(), businessId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));
        }

        Transaction transaction = Transaction.builder()
                .business(businessRef(businessId))
                .type(TransactionType.EXPENSE)
                .amount(request.amount())
                .category(category)
                .note(request.note())
                .paymentMethod(request.paymentMethod())
                .transactionDate(request.transactionDate() != null ? request.transactionDate() : Instant.now())
                .build();

        return transactionMapper.toResponse(transactionRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> list(Long businessId) {
        return transactionRepository.findByBusinessIdOrderByTransactionDateDesc(businessId).stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getById(Long businessId, Long id) {
        Transaction transaction = transactionRepository.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse update(Long businessId, Long id, UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));

        transaction.setAmount(request.amount());
        transaction.setNote(request.note());
        transaction.setPaymentMethod(request.paymentMethod());

        if (transaction.getType() == TransactionType.EXPENSE) {
            ExpenseCategory category = null;
            if (request.categoryId() != null) {
                category = expenseCategoryRepository.findByIdAndBusinessId(request.categoryId(), businessId)
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + request.categoryId()));
            }
            transaction.setCategory(category);
        }

        return transactionMapper.toResponse(transaction);
    }

    @Transactional
    public void delete(Long businessId, Long id) {
        Transaction transaction = transactionRepository.findByIdAndBusinessId(id, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
        transactionRepository.delete(transaction);
    }

    private Business businessRef(Long businessId) {
        return Business.builder().id(businessId).build();
    }
}
