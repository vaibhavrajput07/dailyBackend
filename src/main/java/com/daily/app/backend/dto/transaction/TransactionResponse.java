package com.daily.app.backend.dto.transaction;

import com.daily.app.backend.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        Long categoryId,
        String categoryName,
        String note,
        String paymentMethod,
        Instant transactionDate
) {
}
