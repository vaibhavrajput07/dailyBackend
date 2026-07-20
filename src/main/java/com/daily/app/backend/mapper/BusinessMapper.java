package com.daily.app.backend.mapper;

import com.daily.app.backend.dto.auth.SignupRequest;
import com.daily.app.backend.dto.business.BusinessResponse;
import com.daily.app.backend.entity.Business;
import com.daily.app.backend.entity.User;
import org.springframework.stereotype.Component;

@Component
public class BusinessMapper {

    public Business toEntity(SignupRequest request, User user) {
        return Business.builder()
                .user(user)
                .businessName(request.businessName())
                .businessType(request.businessType())
                .build();
    }

    public BusinessResponse toResponse(Business business) {
        return new BusinessResponse(
                business.getId(),
                business.getBusinessName(),
                business.getBusinessType()
        );
    }
}
