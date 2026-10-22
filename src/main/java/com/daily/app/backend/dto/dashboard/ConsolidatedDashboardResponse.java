package com.daily.app.backend.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ConsolidatedDashboardResponse(
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalSales,
        BigDecimal totalExpenses,
        BigDecimal totalProfit,
        List<BusinessSummary> businesses
) {
}
