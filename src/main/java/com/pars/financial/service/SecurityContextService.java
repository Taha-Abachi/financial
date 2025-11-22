package com.pars.financial.service;

import com.pars.financial.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityContextService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityContextService.class);
    
    private final UserService userService;

    public SecurityContextService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Get the current authenticated user from Spring Security context
     * @return User entity or null if not authenticated
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.debug("No authenticated user found in security context");
            return null;
        }
        
        String username = authentication.getName();
        try {
            User user = userService.getUserEntityByUsername(username);
            if (user != null && user.getRole() != null) {
                logger.debug("Retrieved current user: {} with role: {}", username, user.getRole().getName());
            } else if (user != null) {
                logger.warn("User {} has no role assigned", username);
            }
            return user;
        } catch (Exception e) {
            logger.error("Error getting current user: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if current user is authenticated
     * @return true if user is authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }

    /**
     * Get current user or throw exception if not authenticated
     * @return User entity
     * @throws IllegalStateException if user is not authenticated
     */
    public User getCurrentUserOrThrow() {
        User user = getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return user;
    }
}
