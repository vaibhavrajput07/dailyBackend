package com.daily.app.backend.mapper;

import com.daily.app.backend.dto.category.CategoryResponse;
import com.daily.app.backend.entity.ExpenseCategory;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(ExpenseCategory category) {
        return new CategoryResponse(category.getId(), category.getName(), category.isDefault());
    }
}
