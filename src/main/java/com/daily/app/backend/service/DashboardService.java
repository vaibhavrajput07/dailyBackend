package com.daily.app.backend.service;

import com.daily.app.backend.dto.dashboard.BusinessSummary;
import com.daily.app.backend.dto.dashboard.ConsolidatedDashboardResponse;
import com.daily.app.backend.dto.dashboard.DashboardResponse;
import com.daily.app.backend.entity.Business;
import com.daily.app.backend.entity.TransactionType;
import com.daily.app.backend.exception.ApiException;
import com.daily.app.backend.repository.BusinessRepository;
import com.daily.app.backend.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    private final TransactionRepository transactionRepository;
    private final BusinessRepository businessRepository;

    public DashboardService(TransactionRepository transactionRepository, BusinessRepository businessRepository) {
        this.transactionRepository = transactionRepository;
        this.businessRepository = businessRepository;
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

    /**
     * Totals across every active (non-archived) business the user owns, plus a
     * per-business breakdown. Businesses with no transactions in range are
     * still listed with zero totals. Dates are inclusive IST calendar days;
     * both default to today.
     */
    public ConsolidatedDashboardResponse getConsolidated(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        LocalDate from = startDate != null ? startDate : today;
        LocalDate to = endDate != null ? endDate : from;
        if (to.isBefore(from)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "endDate must not be before startDate");
        }
        Instant start = from.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();

        Map<Long, BigDecimal[]> byBusiness = new HashMap<>();
        for (TransactionRepository.BusinessTypeTotal row
                : transactionRepository.sumActiveBusinessAmountsByTypeInRange(userId, start, end)) {
            BigDecimal[] totals = byBusiness.computeIfAbsent(row.getBusinessId(),
                    id -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (row.getType() == TransactionType.SALE) {
                totals[0] = row.getTotal();
            } else if (row.getType() == TransactionType.EXPENSE) {
                totals[1] = row.getTotal();
            }
        }

        BigDecimal totalSales = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        List<BusinessSummary> summaries = new ArrayList<>();
        for (Business business : businessRepository.findByUserIdAndArchivedFalseOrderByIdAsc(userId)) {
            BigDecimal[] totals = byBusiness.getOrDefault(business.getId(),
                    new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            totalSales = totalSales.add(totals[0]);
            totalExpenses = totalExpenses.add(totals[1]);
            summaries.add(new BusinessSummary(business.getId(), business.getBusinessName(),
                    totals[0], totals[1], totals[0].subtract(totals[1])));
        }

        return new ConsolidatedDashboardResponse(from, to, totalSales, totalExpenses,
                totalSales.subtract(totalExpenses), summaries);
    }
}
