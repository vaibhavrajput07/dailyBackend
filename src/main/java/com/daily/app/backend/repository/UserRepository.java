package com.daily.app.backend.repository;

import com.daily.app.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByMobileNumber(String mobileNumber);

    boolean existsByMobileNumber(String mobileNumber);
}
