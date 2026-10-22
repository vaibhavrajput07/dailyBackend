package com.daily.app.backend.controller;

import com.daily.app.backend.dto.business.BusinessCreateRequest;
import com.daily.app.backend.dto.business.BusinessResponse;
import com.daily.app.backend.dto.business.BusinessUpdateRequest;
import com.daily.app.backend.security.AuthPrincipal;
import com.daily.app.backend.security.BusinessScope;
import com.daily.app.backend.service.BusinessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/business")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping
    public BusinessResponse getBusiness(BusinessScope scope) {
        return businessService.getBusiness(scope.businessId());
    }

    @PutMapping
    public BusinessResponse updateBusiness(BusinessScope scope,
                                            @Valid @RequestBody BusinessUpdateRequest request) {
        return businessService.updateBusiness(scope.userId(), scope.businessId(), request);
    }

    @GetMapping("/all")
    public List<BusinessResponse> listBusinesses(@AuthenticationPrincipal AuthPrincipal principal) {
        return businessService.listForUser(principal.userId());
    }

    @PostMapping
    public ResponseEntity<BusinessResponse> createBusiness(@AuthenticationPrincipal AuthPrincipal principal,
                                                            @Valid @RequestBody BusinessCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(businessService.create(principal.userId(), request));
    }
}
