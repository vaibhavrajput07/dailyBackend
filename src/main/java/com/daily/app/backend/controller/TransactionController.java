package com.daily.app.backend.controller;

import com.daily.app.backend.dto.transaction.AddExpenseRequest;
import com.daily.app.backend.dto.transaction.AddSaleRequest;
import com.daily.app.backend.dto.transaction.TransactionResponse;
import com.daily.app.backend.dto.transaction.UpdateTransactionRequest;
import com.daily.app.backend.security.BusinessScope;
import com.daily.app.backend.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/sale")
    public ResponseEntity<TransactionResponse> addSale(BusinessScope scope,
                                                         @Valid @RequestBody AddSaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.addSale(scope.businessId(), request));
    }

    @PostMapping("/expense")
    public ResponseEntity<TransactionResponse> addExpense(BusinessScope scope,
                                                            @Valid @RequestBody AddExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.addExpense(scope.businessId(), request));
    }

    @GetMapping
    public List<TransactionResponse> list(BusinessScope scope) {
        return transactionService.list(scope.businessId());
    }

    @GetMapping("/{id}")
    public TransactionResponse getById(BusinessScope scope, @PathVariable Long id) {
        return transactionService.getById(scope.businessId(), id);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(BusinessScope scope,
                                       @PathVariable Long id,
                                       @Valid @RequestBody UpdateTransactionRequest request) {
        return transactionService.update(scope.businessId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(BusinessScope scope, @PathVariable Long id) {
        transactionService.delete(scope.businessId(), id);
        return ResponseEntity.noContent().build();
    }
}
