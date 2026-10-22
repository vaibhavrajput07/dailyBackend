package com.daily.app.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

/** Step 1 of signup: checks the mobile/email are free, then emails a verification code. */
public record SignupOtpRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{10}$", message = "mobileNumber must be a 10-digit number") String mobileNumber,
        @NotBlank @Email(message = "email must be a valid email address") @Size(max = 255) String email
) {
    public String normalizedEmail() {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
