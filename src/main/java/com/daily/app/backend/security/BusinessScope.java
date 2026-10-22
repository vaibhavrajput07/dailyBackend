package com.daily.app.backend.security;

/**
 * The business a request operates on, already verified to belong to
 * {@code userId}. Resolved by {@link BusinessScopeArgumentResolver} from the
 * optional {@code X-Business-Id} header, falling back to the business in the
 * JWT so single-business clients behave exactly as before.
 */
public record BusinessScope(
        Long userId,
        Long businessId
) {
}
