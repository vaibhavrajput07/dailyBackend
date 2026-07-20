package com.daily.app.backend.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySummary(
        LocalDate date,
        BigDecimal sales,
        BigDecimal expenses,
        BigDecimal profit
) {
}
