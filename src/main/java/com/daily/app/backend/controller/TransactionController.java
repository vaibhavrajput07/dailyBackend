package com.daily.app.backend.controller;

import com.daily.app.backend.dto.transaction.AddExpenseRequest;
import com.daily.app.backend.dto.transaction.AddSaleRequest;
import com.daily.app.backend.dto.transaction.TransactionResponse;
import com.daily.app.backend.dto.transaction.UpdateTransactionRequest;
import com.daily.app.backend.security.AuthPrincipal;
import com.daily.app.backend.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<TransactionResponse> addSale(@AuthenticationPrincipal AuthPrincipal principal,
                                                         @Valid @RequestBody AddSaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.addSale(principal.businessId(), request));
    }

    @PostMapping("/expense")
    public ResponseEntity<TransactionResponse> addExpense(@AuthenticationPrincipal AuthPrincipal principal,
                                                            @Valid @RequestBody AddExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.addExpense(principal.businessId(), request));
    }

    @GetMapping
    public List<TransactionResponse> list(@AuthenticationPrincipal AuthPrincipal principal) {
        return transactionService.list(principal.businessId());
    }

    @GetMapping("/{id}")
    public TransactionResponse getById(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        return transactionService.getById(principal.businessId(), id);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@AuthenticationPrincipal AuthPrincipal principal,
                                       @PathVariable Long id,
                                       @Valid @RequestBody UpdateTransactionRequest request) {
        return transactionService.update(principal.businessId(), id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthPrincipal principal, @PathVariable Long id) {
        transactionService.delete(principal.businessId(), id);
        return ResponseEntity.noContent().build();
    }
}
