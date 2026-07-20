package com.daily.app.backend.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardResponse(
        LocalDate date,
        BigDecimal todaySales,
        BigDecimal todayExpenses,
        BigDecimal todayProfit
) {
}
