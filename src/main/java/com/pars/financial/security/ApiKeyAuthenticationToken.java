package com.pars.financial.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import com.pars.financial.entity.User;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {
    private final String apiKey;
    private final Long userId;
    private final User user;

    // Constructor for unauthenticated state
    public ApiKeyAuthenticationToken(String apiKey, User user) {
        super(null);
        this.apiKey = apiKey;
        this.user = user;
        this.userId = null;
        setAuthenticated(false);
    }

    // Constructor for authenticated state
    public ApiKeyAuthenticationToken(String apiKey, Long userId, Collection<? extends GrantedAuthority> authorities, User user) {
        super(authorities);
        this.apiKey = apiKey;
        this.userId = userId;
        this.user = user;
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

    public User getUser() {
        return user;
    }
}