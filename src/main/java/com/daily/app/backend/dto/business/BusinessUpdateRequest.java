package com.daily.app.backend.dto.business;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BusinessUpdateRequest(
        @NotBlank @Size(max = 150) String businessName
) {
}
