package com.daily.app.backend.dto.business;

import com.daily.app.backend.entity.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * clientId is the client's local id for a business created offline; resending
 * the same clientId returns the existing business instead of a duplicate.
 */
public record BusinessCreateRequest(
        @NotBlank @Size(max = 150) String businessName,
        @NotNull BusinessType businessType,
        @Size(max = 36) String clientId
) {
}
