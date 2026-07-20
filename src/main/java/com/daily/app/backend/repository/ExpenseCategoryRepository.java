package com.daily.app.backend.repository;

import com.daily.app.backend.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {

    Optional<ExpenseCategory> findByIdAndBusinessId(Long id, Long businessId);

    List<ExpenseCategory> findByBusinessIdOrderByNameAsc(Long businessId);

    long countByBusinessId(Long businessId);

    boolean existsByBusinessIdAndNameIgnoreCase(Long businessId, String name);
}
