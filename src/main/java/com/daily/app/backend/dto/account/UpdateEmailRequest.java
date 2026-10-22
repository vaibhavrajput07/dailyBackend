package com.daily.app.backend.dto.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record UpdateEmailRequest(
        @NotBlank @Email(message = "email must be a valid email address") @Size(max = 255) String email,
        @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "otp must be the 6-digit code from the email") String otp
) {
    public String normalizedEmail() {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
