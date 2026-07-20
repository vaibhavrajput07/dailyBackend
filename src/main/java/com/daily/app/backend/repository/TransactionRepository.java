package com.daily.app.backend.repository;

import com.daily.app.backend.entity.Transaction;
import com.daily.app.backend.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBusinessIdOrderByTransactionDateDesc(Long businessId);

    Optional<Transaction> findByIdAndBusinessId(Long id, Long businessId);

    interface TypeTotal {
        TransactionType getType();

        java.math.BigDecimal getTotal();
    }

    @Query("select t.type as type, coalesce(sum(t.amount), 0) as total " +
            "from Transaction t " +
            "where t.business.id = :businessId and t.transactionDate >= :start and t.transactionDate < :end " +
            "group by t.type")
    List<TypeTotal> sumAmountsByTypeInRange(@Param("businessId") Long businessId,
                                             @Param("start") Instant start,
                                             @Param("end") Instant end);

    interface DailyTypeTotal {
        java.sql.Date getDay();

        TransactionType getType();

        java.math.BigDecimal getTotal();
    }

    @Query("select function('date', t.transactionDate) as day, t.type as type, coalesce(sum(t.amount), 0) as total " +
            "from Transaction t " +
            "where t.business.id = :businessId and t.transactionDate >= :start and t.transactionDate < :end " +
            "group by function('date', t.transactionDate), t.type " +
            "order by function('date', t.transactionDate)")
    List<DailyTypeTotal> sumAmountsByDayAndTypeInRange(@Param("businessId") Long businessId,
                                                         @Param("start") Instant start,
                                                         @Param("end") Instant end);
}
