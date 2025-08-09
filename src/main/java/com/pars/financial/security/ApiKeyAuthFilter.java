package com.pars.financial.security;

import java.io.IOException;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.pars.financial.entity.ApiUser;
import com.pars.financial.enums.UserRole;
import com.pars.financial.repository.ApiUserRepository;
import com.pars.financial.utils.ApiKeyEncryption;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private final String apiKeyHeader = "X-API-KEY";
    private final ApiUserRepository apiUserRepository;
    private final ApiKeyEncryption apiKeyEncryption;

    public ApiKeyAuthFilter(ApiUserRepository apiKeyRepository, ApiKeyEncryption apiKeyEncryption) {
        this.apiUserRepository = apiKeyRepository;
        this.apiKeyEncryption = apiKeyEncryption;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
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
            ApiUser apiUser = apiUserRepository.findByApiKey(encryptedApiKey);
            if (apiUser != null) {
                // API key is valid; set authentication with userId
                ApiKeyAuthenticationToken auth = new ApiKeyAuthenticationToken(
                        apiKey,
                        apiUser.getId(),
                        AuthorityUtils.createAuthorityList(apiUser.getUserRole() == UserRole.API_USER ? "ROLE_API_USER" : "ROLE_ADMIN"),
                        apiUser
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        filterChain.doFilter(request, response);
    }
}