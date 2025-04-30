package com.pars.financial.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pars.financial.entity.ApiUser;
import com.pars.financial.security.ApiKeyAuthenticationToken;

public class ApiUserUtil {
    public static ApiUser getApiUser() {
        ApiUser apiUser = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof ApiKeyAuthenticationToken token) {
            apiUser = token.getApiUser();
        }
        return apiUser;
    }
}
