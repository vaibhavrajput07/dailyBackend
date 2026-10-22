package com.daily.app.backend.dto.dashboard;

import java.math.BigDecimal;

public record BusinessSummary(
        Long businessId,
        String businessName,
        BigDecimal sales,
        BigDecimal expenses,
        BigDecimal profit
) {
}
