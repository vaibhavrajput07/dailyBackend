package com.daily.app.backend.dto.business;

import com.daily.app.backend.entity.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * businessType and archived are optional so pre-multi-business clients that
 * only send businessName keep working unchanged.
 */
public record BusinessUpdateRequest(
        @NotBlank @Size(max = 150) String businessName,
        BusinessType businessType,
        Boolean archived
) {
}
