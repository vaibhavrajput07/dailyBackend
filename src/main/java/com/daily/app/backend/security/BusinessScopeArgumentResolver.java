package com.daily.app.backend.security;

import com.daily.app.backend.exception.ApiException;
import com.daily.app.backend.exception.ResourceNotFoundException;
import com.daily.app.backend.repository.BusinessRepository;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class BusinessScopeArgumentResolver implements HandlerMethodArgumentResolver {

    public static final String HEADER = "X-Business-Id";

    private final BusinessRepository businessRepository;

    public BusinessScopeArgumentResolver(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return BusinessScope.class.equals(parameter.getParameterType());
    }

    @Override
    public BusinessScope resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String header = webRequest.getHeader(HEADER);
        if (header == null || header.isBlank()) {
            return new BusinessScope(principal.userId(), principal.businessId());
        }

        Long businessId;
        try {
            businessId = Long.valueOf(header.trim());
        } catch (NumberFormatException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid " + HEADER + " header");
        }

        // 404 rather than 403 so another user's business ids can't be probed.
        if (!businessRepository.existsByIdAndUserId(businessId, principal.userId())) {
            throw new ResourceNotFoundException("Business not found: " + businessId);
        }
        return new BusinessScope(principal.userId(), businessId);
    }
}
