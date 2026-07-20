package com.daily.app.backend.dto.business;

import com.daily.app.backend.entity.BusinessType;

public record BusinessResponse(
        Long id,
        String businessName,
        BusinessType businessType
) {
}
