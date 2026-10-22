package com.daily.app.backend.security;

import com.daily.app.backend.exception.ApiException;
import com.daily.app.backend.exception.ResourceNotFoundException;
import com.daily.app.backend.repository.BusinessRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BusinessScopeArgumentResolverTest {

    private static final long USER_ID = 7L;
    private static final long LOGIN_BUSINESS_ID = 70L;

    private final BusinessRepository businessRepository = mock(BusinessRepository.class);
    private final BusinessScopeArgumentResolver resolver = new BusinessScopeArgumentResolver(businessRepository);
    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @BeforeEach
    void authenticate() {
        AuthPrincipal principal = new AuthPrincipal(USER_ID, LOGIN_BUSINESS_ID, "9876543210");
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private BusinessScope resolve() {
        return resolver.resolveArgument(null, null, new ServletWebRequest(request), null);
    }

    @Test
    void withoutHeaderFallsBackToLoginBusiness() {
        BusinessScope scope = resolve();

        assertThat(scope).isEqualTo(new BusinessScope(USER_ID, LOGIN_BUSINESS_ID));
        verifyNoInteractions(businessRepository);
    }

    @Test
    void headerSelectsAnotherOwnedBusiness() {
        request.addHeader(BusinessScopeArgumentResolver.HEADER, "71");
        when(businessRepository.existsByIdAndUserId(71L, USER_ID)).thenReturn(true);

        assertThat(resolve()).isEqualTo(new BusinessScope(USER_ID, 71L));
    }

    @Test
    void headerForAnotherUsersBusinessIsNotFound() {
        request.addHeader(BusinessScopeArgumentResolver.HEADER, "999");
        when(businessRepository.existsByIdAndUserId(999L, USER_ID)).thenReturn(false);

        assertThatThrownBy(this::resolve).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void malformedHeaderIsBadRequest() {
        request.addHeader(BusinessScopeArgumentResolver.HEADER, "abc");

        assertThatThrownBy(this::resolve)
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
