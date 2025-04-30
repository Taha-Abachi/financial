package com.pars.financial.security;

import com.pars.financial.entity.ApiUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import java.util.Collection;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
    private final String apiKey;
    private final Long userId;
    private final ApiUser apiUser;

    // Constructor for unauthenticated state
    public ApiKeyAuthenticationToken(String apiKey, ApiUser apiUser) {
        super(null);
        this.apiKey = apiKey;
        this.apiUser = apiUser;
        this.userId = null;
        setAuthenticated(false);
    }

    // Constructor for authenticated state
    public ApiKeyAuthenticationToken(String apiKey, Long userId, Collection<? extends GrantedAuthority> authorities, ApiUser apiUser) {
        super(authorities);
        this.apiKey = apiKey;
        this.userId = userId;
        this.apiUser = apiUser;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null; // API key is not a credential like a password
    }

    @Override
    public Object getPrincipal() {
        return userId; // Return userId as the principal
    }

    public String getApiKey() {
        return apiKey;
    }

    public ApiUser getApiUser() {
        return apiUser;
    }
}