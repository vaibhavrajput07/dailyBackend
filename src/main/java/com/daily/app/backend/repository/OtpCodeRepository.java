package com.daily.app.backend.repository;

import com.daily.app.backend.entity.OtpCode;
import com.daily.app.backend.entity.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {

    Optional<OtpCode> findFirstByEmailAndPurposeOrderByCreatedAtDescIdDesc(String email, OtpPurpose purpose);
}
