package com.daily.app.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ForgotPasswordRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "mobileNumber must be a 10-digit number") String mobileNumber
) {
}
