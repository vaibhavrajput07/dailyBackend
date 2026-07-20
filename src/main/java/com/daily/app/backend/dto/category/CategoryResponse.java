package com.daily.app.backend.dto.category;

public record CategoryResponse(
        Long id,
        String name,
        boolean isDefault
) {
}
