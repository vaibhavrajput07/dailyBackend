package com.daily.app.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "mobileNumber must be a 10-digit number") String mobileNumber,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "otp must be the 6-digit code from the email") String otp,
        @NotBlank @Size(min = 6, max = 72) String newPassword
) {
}
