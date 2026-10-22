package com.daily.app.backend.controller;

import com.daily.app.backend.dto.category.CategoryRequest;
import com.daily.app.backend.dto.category.CategoryResponse;
import com.daily.app.backend.security.BusinessScope;
import com.daily.app.backend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public List<CategoryResponse> list(BusinessScope scope) {
        return categoryService.list(scope.businessId());
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(BusinessScope scope,
                                                     @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(scope.businessId(), request));
    }

    @PutMapping("/{id}")
    public CategoryResponse rename(BusinessScope scope,
                                    @PathVariable Long id,
                                    @Valid @RequestBody CategoryRequest request) {
        return categoryService.rename(scope.businessId(), id, request);
    }
}
