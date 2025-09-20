package com.pars.financial.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Custom security expressions for method-level security.
 * This provides a clean way to check for SUPERADMIN access.
 */
@Component("securityExpressions")
public class SecurityExpressions {
    
    /**
     * Check if the current user has SUPERADMIN role.
     * Can be used in @PreAuthorize annotations.
     */
    public boolean isSuperAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> "ROLE_SUPERADMIN".equals(authority));
    }
    
    /**
     * Check if the current user has ADMIN or SUPERADMIN role.
     * Can be used in @PreAuthorize annotations.
     */
    public boolean isAdminOrSuperAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> 
                    "ROLE_ADMIN".equals(authority) || 
                    "ROLE_SUPERADMIN".equals(authority)
                );
    }
}
