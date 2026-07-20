package com.daily.app.backend.service;

import com.daily.app.backend.dto.report.DailySummary;
import com.daily.app.backend.dto.report.ReportResponse;
import com.daily.app.backend.entity.TransactionType;
import com.daily.app.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.daily.app.backend.service.DashboardService.BUSINESS_ZONE;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;

    public ReportService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public ReportResponse getMonthly(Long businessId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        return getRange(businessId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
    }

    public ReportResponse getRange(Long businessId, LocalDate startDate, LocalDate endDate) {
        Instant start = startDate.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();

        Map<LocalDate, BigDecimal[]> byDay = new LinkedHashMap<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            byDay.put(d, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }

        for (TransactionRepository.DailyTypeTotal row : transactionRepository.sumAmountsByDayAndTypeInRange(businessId, start, end)) {
            LocalDate day = row.getDay().toLocalDate();
            BigDecimal[] totals = byDay.computeIfAbsent(day, d -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (row.getType() == TransactionType.SALE) {
                totals[0] = row.getTotal();
            } else if (row.getType() == TransactionType.EXPENSE) {
                totals[1] = row.getTotal();
            }
        }

        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        List<DailySummary> daily = new java.util.ArrayList<>();

        for (Map.Entry<LocalDate, BigDecimal[]> entry : byDay.entrySet()) {
            BigDecimal sales = entry.getValue()[0];
            BigDecimal expenses = entry.getValue()[1];
            totalSales = totalSales.add(sales);
            totalExpenses = totalExpenses.add(expenses);
            daily.add(new DailySummary(entry.getKey(), sales, expenses, sales.subtract(expenses)));
        }

        return new ReportResponse(startDate, endDate, totalSales, totalExpenses, totalSales.subtract(totalExpenses), daily);
    }
}
