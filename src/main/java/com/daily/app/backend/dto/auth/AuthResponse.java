package com.daily.app.backend.dto.auth;

public record AuthResponse(
        String token,
        Long userId,
        Long businessId,
        String businessName,
        String ownerName
) {
}
