package com.daily.app.backend.dto.auth;

import com.daily.app.backend.entity.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Size(max = 150) String businessName,
        @NotBlank @Size(max = 100) String ownerName,
        @NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "mobileNumber must be a 10-digit number") String mobileNumber,
        @NotBlank @Size(min = 6, max = 72) String password,
        @NotNull BusinessType businessType
) {
}
