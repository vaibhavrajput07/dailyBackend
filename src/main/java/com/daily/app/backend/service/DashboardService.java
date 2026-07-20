package com.daily.app.backend.service;

import com.daily.app.backend.dto.dashboard.DashboardResponse;
import com.daily.app.backend.entity.TransactionType;
import com.daily.app.backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class DashboardService {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    private final TransactionRepository transactionRepository;

    public DashboardService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public DashboardResponse getTodayDashboard(Long businessId) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        Instant start = today.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();

        BigDecimal sales = BigDecimal.ZERO;
        BigDecimal expenses = BigDecimal.ZERO;

        for (TransactionRepository.TypeTotal row : transactionRepository.sumAmountsByTypeInRange(businessId, start, end)) {
            if (row.getType() == TransactionType.SALE) {
                sales = row.getTotal();
            } else if (row.getType() == TransactionType.EXPENSE) {
                expenses = row.getTotal();
            }
        }

        return new DashboardResponse(today, sales, expenses, sales.subtract(expenses));
    }
}
