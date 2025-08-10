package com.pars.financial.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import com.pars.financial.entity.User;
import com.pars.financial.security.ApiKeyAuthenticationToken;

public class ApiUserUtil {
    public static User getApiUser() {
        User apiUser = null;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof ApiKeyAuthenticationToken token) {
            apiUser = token.getUser();
        }
        return apiUser;
    }
}
