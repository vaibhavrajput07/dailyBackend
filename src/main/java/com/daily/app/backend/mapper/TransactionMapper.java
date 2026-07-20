package com.daily.app.backend.mapper;

import com.daily.app.backend.dto.transaction.TransactionResponse;
import com.daily.app.backend.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getCategory() != null ? transaction.getCategory().getId() : null,
                transaction.getCategory() != null ? transaction.getCategory().getName() : null,
                transaction.getNote(),
                transaction.getPaymentMethod(),
                transaction.getTransactionDate()
        );
    }
}
