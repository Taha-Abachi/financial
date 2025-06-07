package com.pars.financial.security;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pars.financial.configuration.RateLimitProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


public class RateLimitFilter extends OncePerRequestFilter {
   
    private static final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastResetTime = new ConcurrentHashMap<>();
    private static final Map<String, Long> banEndTime = new ConcurrentHashMap<>();
    private final RateLimitProperties properties;

    public RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-KEY");
        if (apiKey != null) {
            if (isBanned(apiKey)) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("You are temporarily banned. Please try again later.");
                return;
            }
            
            if (!isRateLimitExceeded(apiKey)) {
                filterChain.doFilter(request, response);
            } else {
                banApiKey(apiKey);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded. You have been temporarily banned.");
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean isRateLimitExceeded(String apiKey) {
        long currentTime = System.currentTimeMillis();
        long lastReset = lastResetTime.getOrDefault(apiKey, currentTime);

        // Reset counter if window has passed
        if (currentTime - lastReset >= properties.getWindowMilliSeconds()) {
            requestCounts.remove(apiKey);
            lastResetTime.remove(apiKey);
            return false;
        }

        // Get or create counter for this API key
        AtomicInteger count = requestCounts.computeIfAbsent(apiKey, k -> {
            lastResetTime.put(k, currentTime);
            return new AtomicInteger(0);
        });

        // Check if we've exceeded the limit
        return count.incrementAndGet() > properties.getMaxRequests();
    }                                           

    private boolean isBanned(String apiKey) {
        Long banEnd = banEndTime.get(apiKey);
        if (banEnd == null) {
            return false;
        }
        
        if (System.currentTimeMillis() > banEnd) {
            banEndTime.remove(apiKey);
            return false;
        }
        
        return true;
    }

    private void banApiKey(String apiKey) {
        banEndTime.put(apiKey, System.currentTimeMillis() + properties.getBanPeriodMilliSeconds());
    }
}