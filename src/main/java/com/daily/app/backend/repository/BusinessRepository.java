package com.daily.app.backend.repository;

import com.daily.app.backend.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    List<Business> findByUserIdOrderByIdAsc(Long userId);

    List<Business> findByUserIdAndArchivedFalseOrderByIdAsc(Long userId);

    Optional<Business> findFirstByUserIdAndArchivedFalseOrderByIdAsc(Long userId);

    Optional<Business> findFirstByUserIdOrderByIdAsc(Long userId);

    Optional<Business> findByUserIdAndClientId(Long userId, String clientId);

    boolean existsByIdAndUserId(Long id, Long userId);

    long countByUserIdAndArchivedFalse(Long userId);
}
