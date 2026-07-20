package com.daily.app.backend.security;

public record AuthPrincipal(
        Long userId,
        Long businessId,
        String mobileNumber
) {
}
