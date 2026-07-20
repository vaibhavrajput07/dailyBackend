package com.daily.app.backend.dto.transaction;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateTransactionRequest(
        @NotNull @DecimalMin(value = "0.01") @DecimalMax(value = "9999999999.99") BigDecimal amount,
        Long categoryId,
        @Size(max = 255) String note,
        @Size(max = 20) String paymentMethod
) {
}
