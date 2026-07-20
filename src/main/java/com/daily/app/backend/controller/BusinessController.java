package com.daily.app.backend.controller;

import com.daily.app.backend.dto.business.BusinessResponse;
import com.daily.app.backend.dto.business.BusinessUpdateRequest;
import com.daily.app.backend.security.AuthPrincipal;
import com.daily.app.backend.service.BusinessService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping
    public BusinessResponse getBusiness(@AuthenticationPrincipal AuthPrincipal principal) {
        return businessService.getBusiness(principal.businessId());
    }

    @PutMapping
    public BusinessResponse updateBusiness(@AuthenticationPrincipal AuthPrincipal principal,
                                            @Valid @RequestBody BusinessUpdateRequest request) {
        return businessService.updateBusiness(principal.businessId(), request);
    }
}
