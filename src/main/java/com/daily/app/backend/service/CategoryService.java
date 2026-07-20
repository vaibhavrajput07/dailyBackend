package com.daily.app.backend.service;

import com.daily.app.backend.dto.category.CategoryRequest;
import com.daily.app.backend.dto.category.CategoryResponse;
import com.daily.app.backend.entity.Business;
import com.daily.app.backend.entity.ExpenseCategory;
import com.daily.app.backend.exception.DuplicateResourceException;
import com.daily.app.backend.exception.ResourceNotFoundException;
import com.daily.app.backend.mapper.CategoryMapper;
import com.daily.app.backend.repository.BusinessRepository;
import com.daily.app.backend.repository.ExpenseCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final BusinessRepository businessRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(ExpenseCategoryRepository expenseCategoryRepository,
                            BusinessRepository businessRepository,
                            CategoryMapper categoryMapper) {
        this.expenseCategoryRepository = expenseCategoryRepository;
        this.businessRepository = businessRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public List<CategoryResponse> list(Long businessId) {
        if (expenseCategoryRepository.countByBusinessId(businessId) == 0) {
            seedDefaults(businessId);
        }
        return expenseCategoryRepository.findByBusinessIdOrderByNameAsc(businessId).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(Long businessId, CategoryRequest request) {
        if (expenseCategoryRepository.existsByBusinessIdAndNameIgnoreCase(businessId, request.name())) {
            throw new DuplicateResourceException("Category already exists: " + request.name());
        }

        ExpenseCategory category = ExpenseCategory.builder()
                .business(Business.builder().id(businessId).build())
                .name(request.name())
                .isDefault(false)
                .build();

        return categoryMapper.toResponse(expenseCategoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse rename(Long businessId, Long categoryId, CategoryRequest request) {
        ExpenseCategory category = expenseCategoryRepository.findByIdAndBusinessId(categoryId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

        if (!category.getName().equalsIgnoreCase(request.name())
                && expenseCategoryRepository.existsByBusinessIdAndNameIgnoreCase(businessId, request.name())) {
            throw new DuplicateResourceException("Category already exists: " + request.name());
        }

        category.setName(request.name());
        return categoryMapper.toResponse(category);
    }

    private void seedDefaults(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessId));

        List<ExpenseCategory> defaults = DefaultCategorySeeder.defaultsFor(business.getBusinessType()).stream()
                .map(name -> ExpenseCategory.builder()
                        .business(business)
                        .name(name)
                        .isDefault(true)
                        .build())
                .toList();

        expenseCategoryRepository.saveAll(defaults);
    }
}
