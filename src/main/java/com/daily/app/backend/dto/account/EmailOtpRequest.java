package com.daily.app.backend.dto.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record EmailOtpRequest(
        @NotBlank @Email(message = "email must be a valid email address") @Size(max = 255) String email
) {
    public String normalizedEmail() {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
