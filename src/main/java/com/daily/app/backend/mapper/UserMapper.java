package com.daily.app.backend.mapper;

import com.daily.app.backend.dto.auth.SignupRequest;
import com.daily.app.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(SignupRequest request, String passwordHash) {
        return User.builder()
                .ownerName(request.ownerName())
                .mobileNumber(request.mobileNumber())
                .email(request.normalizedEmail())
                .passwordHash(passwordHash)
                .build();
    }
}
