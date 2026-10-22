package com.daily.app.backend.service;

import com.daily.app.backend.dto.business.BusinessCreateRequest;
import com.daily.app.backend.dto.business.BusinessResponse;
import com.daily.app.backend.dto.business.BusinessUpdateRequest;
import com.daily.app.backend.entity.Business;
import com.daily.app.backend.exception.ApiException;
import com.daily.app.backend.exception.ResourceNotFoundException;
import com.daily.app.backend.mapper.BusinessMapper;
import com.daily.app.backend.repository.BusinessRepository;
import com.daily.app.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final BusinessMapper businessMapper;

    public BusinessService(BusinessRepository businessRepository,
                            UserRepository userRepository,
                            BusinessMapper businessMapper) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.businessMapper = businessMapper;
    }

    public BusinessResponse getBusiness(Long businessId) {
        return businessMapper.toResponse(findBusiness(businessId));
    }

    public List<BusinessResponse> listForUser(Long userId) {
        return businessRepository.findByUserIdOrderByIdAsc(userId).stream()
                .map(businessMapper::toResponse)
                .toList();
    }

    /**
     * Idempotent on clientId: a retried offline create (e.g. the first
     * response was lost) returns the business already created for it.
     */
    @Transactional
    public BusinessResponse create(Long userId, BusinessCreateRequest request) {
        if (request.clientId() != null) {
            Optional<Business> existing = businessRepository.findByUserIdAndClientId(userId, request.clientId());
            if (existing.isPresent()) {
                return businessMapper.toResponse(existing.get());
            }
        }
        Business business = businessMapper.toEntity(request, userRepository.getReferenceById(userId));
        return businessMapper.toResponse(businessRepository.save(business));
    }

    /** {@code userId} must own {@code businessId} — guaranteed by BusinessScope resolution. */
    @Transactional
    public BusinessResponse updateBusiness(Long userId, Long businessId, BusinessUpdateRequest request) {
        Business business = findBusiness(businessId);
        business.setBusinessName(request.businessName());
        if (request.businessType() != null) {
            business.setBusinessType(request.businessType());
        }
        if (request.archived() != null && request.archived() != business.isArchived()) {
            if (request.archived() && businessRepository.countByUserIdAndArchivedFalse(userId) <= 1) {
                throw new ApiException(HttpStatus.CONFLICT, "At least one business must stay active");
            }
            business.setArchived(request.archived());
        }
        return businessMapper.toResponse(business);
    }

    private Business findBusiness(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found: " + businessId));
    }
}
