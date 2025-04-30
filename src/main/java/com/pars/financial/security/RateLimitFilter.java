package com.pars.financial.security;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RateLimitFilter extends OncePerRequestFilter {
    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastResetTime = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-KEY");
        if (apiKey != null) {
            if (!isRateLimitExceeded(apiKey)) {
                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded. Please try again later.");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isRateLimitExceeded(String apiKey) {
        long currentTime = System.currentTimeMillis();
        long lastReset = lastResetTime.getOrDefault(apiKey, currentTime);

        if (currentTime - lastReset > 60000) { // 1 minute
            requestCounts.put(apiKey, new AtomicInteger(0));
            lastResetTime.put(apiKey, currentTime);
        }

        AtomicInteger count = requestCounts.computeIfAbsent(apiKey, k -> new AtomicInteger(0));
        return count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE;
    }
} 