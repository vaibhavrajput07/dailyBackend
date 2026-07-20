package com.daily.app.backend.service;

import com.daily.app.backend.dto.business.BusinessResponse;
import com.daily.app.backend.dto.business.BusinessUpdateRequest;
import com.daily.app.backend.entity.Business;
import com.daily.app.backend.exception.ResourceNotFoundException;
import com.daily.app.backend.mapper.BusinessMapper;
import com.daily.app.backend.repository.BusinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessMapper businessMapper;

    public BusinessService(BusinessRepository businessRepository, BusinessMapper businessMapper) {
        this.businessRepository = businessRepository;
        this.businessMapper = businessMapper;
    }

    public BusinessResponse getBusiness(Long businessId) {
        return businessMapper.toResponse(findBusiness(businessId));
    }

    @Transactional
    public BusinessResponse updateBusiness(Long businessId, BusinessUpdateRequest request) {
        Business business = findBusiness(businessId);
        business.setBusinessName(request.businessName());
        return businessMapper.toResponse(business);
    }

    private Business findBusiness(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessId));
    }
}
