package com.daily.app.backend.controller;

import com.daily.app.backend.dto.category.CategoryRequest;
import com.daily.app.backend.dto.category.CategoryResponse;
import com.daily.app.backend.security.AuthPrincipal;
import com.daily.app.backend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/settings/categories")
public class SettingsController {

    private final CategoryService categoryService;

    public SettingsController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> list(@AuthenticationPrincipal AuthPrincipal principal) {
        return categoryService.list(principal.businessId());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@AuthenticationPrincipal AuthPrincipal principal,
                                                     @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(principal.businessId(), request));
    }

    @PutMapping("/{id}")
    public CategoryResponse rename(@AuthenticationPrincipal AuthPrincipal principal,
                                    @PathVariable Long id,
                                    @Valid @RequestBody CategoryRequest request) {
        return categoryService.rename(principal.businessId(), id, request);
    }
}
