package com.daily.app.backend.service;

import com.daily.app.backend.dto.dashboard.BusinessSummary;
import com.daily.app.backend.dto.dashboard.ConsolidatedDashboardResponse;
import com.daily.app.backend.entity.Business;
import com.daily.app.backend.entity.BusinessType;
import com.daily.app.backend.entity.TransactionType;
import com.daily.app.backend.exception.ApiException;
import com.daily.app.backend.repository.BusinessRepository;
import com.daily.app.backend.repository.TransactionRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private static final long USER_ID = 7L;

    private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
    private final BusinessRepository businessRepository = mock(BusinessRepository.class);
    private final DashboardService service = new DashboardService(transactionRepository, businessRepository);

    private static Business business(long id, String name) {
        return Business.builder().id(id).businessName(name).businessType(BusinessType.GROCERY).build();
    }

    private static TransactionRepository.BusinessTypeTotal row(long businessId, TransactionType type, String total) {
        return new TransactionRepository.BusinessTypeTotal() {
            @Override
            public Long getBusinessId() {
                return businessId;
            }

            @Override
            public TransactionType getType() {
                return type;
            }

            @Override
            public BigDecimal getTotal() {
                return new BigDecimal(total);
            }
        };
    }

    @Test
    void consolidatedSumsEachBusinessOnceAndListsEmptyBusinesses() {
        LocalDate day = LocalDate.of(2026, 10, 21);
        Instant start = day.atStartOfDay(DashboardService.BUSINESS_ZONE).toInstant();
        Instant end = day.plusDays(1).atStartOfDay(DashboardService.BUSINESS_ZONE).toInstant();
        when(businessRepository.findByUserIdAndArchivedFalseOrderByIdAsc(USER_ID)).thenReturn(List.of(
                business(1L, "Clothes Shop"), business(2L, "Car Showroom"), business(3L, "Medical Store")));
        when(transactionRepository.sumActiveBusinessAmountsByTypeInRange(USER_ID, start, end)).thenReturn(List.of(
                row(1L, TransactionType.SALE, "1000.00"),
                row(1L, TransactionType.EXPENSE, "250.00"),
                row(2L, TransactionType.SALE, "50000.00")));

        ConsolidatedDashboardResponse response = service.getConsolidated(USER_ID, day, day);

        assertThat(response.totalSales()).isEqualByComparingTo("51000.00");
        assertThat(response.totalExpenses()).isEqualByComparingTo("250.00");
        assertThat(response.totalProfit()).isEqualByComparingTo("50750.00");
        assertThat(response.businesses()).extracting(BusinessSummary::businessName)
                .containsExactly("Clothes Shop", "Car Showroom", "Medical Store");
        assertThat(response.businesses().get(0).profit()).isEqualByComparingTo("750.00");
        assertThat(response.businesses().get(2).sales()).isEqualByComparingTo("0");
    }

    @Test
    void consolidatedRejectsInvertedRange() {
        assertThatThrownBy(() -> service.getConsolidated(USER_ID, LocalDate.of(2026, 10, 21), LocalDate.of(2026, 10, 1)))
                .isInstanceOf(ApiException.class);
    }
}
