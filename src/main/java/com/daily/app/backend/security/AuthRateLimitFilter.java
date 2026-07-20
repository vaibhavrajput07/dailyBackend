package com.daily.app.backend.security;

import com.daily.app.backend.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Limits brute-forceable public auth endpoints to a small number of attempts per IP per minute.
 * Deliberately a plain in-memory fixed-window counter (no external dependency) since this runs
 * as a single backend instance — revisit if the backend is ever scaled horizontally.
 */
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final long WINDOW_MILLIS = 60_000;
    private static final int MAX_REQUESTS_PER_WINDOW = 5;

    private final ConcurrentMap<String, RateLimitWindow> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public AuthRateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return !("/auth/login".equals(path) || "/auth/signup".equals(path));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String key = clientKey(request) + ":" + request.getRequestURI();
        long now = System.currentTimeMillis();
        RateLimitWindow window = windows.computeIfAbsent(key, k -> new RateLimitWindow(now));

        if (window.tryConsume(now, WINDOW_MILLIS, MAX_REQUESTS_PER_WINDOW)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse body = new ErrorResponse(HttpStatus.TOO_MANY_REQUESTS.value(), "Too Many Requests",
                "Too many attempts. Please wait a minute and try again.");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private String clientKey(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
