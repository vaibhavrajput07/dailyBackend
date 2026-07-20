package com.daily.app.backend.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalSales,
        BigDecimal totalExpenses,
        BigDecimal totalProfit,
        List<DailySummary> dailyBreakdown
) {
}
