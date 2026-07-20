package com.daily.app.backend.repository;

import com.daily.app.backend.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessRepository extends JpaRepository<Business, Long> {

    Optional<Business> findByUserId(Long userId);
}
