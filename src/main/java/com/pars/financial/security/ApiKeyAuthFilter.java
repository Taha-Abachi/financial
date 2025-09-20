package com.pars.financial.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pars.financial.entity.User;
import com.pars.financial.repository.UserRepository;
import com.pars.financial.utils.ApiKeyEncryption;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private final String apiKeyHeader = "X-API-KEY";
    private final UserRepository userRepository;
    private final ApiKeyEncryption apiKeyEncryption;

    public ApiKeyAuthFilter(UserRepository userRepository, ApiKeyEncryption apiKeyEncryption) {
        this.userRepository = userRepository;
        this.apiKeyEncryption = apiKeyEncryption;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip API key processing if JWT authentication is already present
        if (SecurityContextHolder.getContext().getAuthentication() != null && 
            SecurityContextHolder.getContext().getAuthentication() instanceof org.springframework.security.authentication.UsernamePasswordAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String apiKey = request.getHeader(apiKeyHeader);
        if (apiKey != null) {
            String encryptedApiKey = apiKeyEncryption.encrypt(apiKey);
            User user = userRepository.findByApiKey(encryptedApiKey).orElse(null);
            if (user != null && user.isActive() && user.canUseApiKey()) {
                // API key is valid; set authentication with userId
                String roleName = "ROLE_" + user.getRole().getName();
                ApiKeyAuthenticationToken auth = new ApiKeyAuthenticationToken(
                        apiKey,
                        user.getId(),
                        AuthorityUtils.createAuthorityList(roleName),
                        user
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}