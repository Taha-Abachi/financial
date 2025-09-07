package com.pars.financial.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.pars.financial.entity.User;
import com.pars.financial.security.ApiKeyAuthenticationToken;
import com.pars.financial.repository.UserRepository;
import com.pars.financial.dto.GenericResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ApiUserUtil {
    
    private static UserRepository userRepository;
    
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        ApiUserUtil.userRepository = userRepository;
    }
    
    public static User getApiUser() {
        return getApiUser(userRepository);
    }
    
    // Package-private method for testing
    static User getApiUser(UserRepository repository) {
        User apiUser = null;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication instanceof ApiKeyAuthenticationToken token) {
            // Handle API key authentication
            apiUser = token.getUser();
        } else if (authentication instanceof UsernamePasswordAuthenticationToken token) {
            // Handle JWT authentication
            Object principal = token.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                // Get the username from UserDetails and fetch the User entity
                String username = userDetails.getUsername();
                if (repository != null) {
                    apiUser = repository.findByUsername(username).orElse(null);
                }
            }
        }
        
        return apiUser;
    }
    
    /**
     * Gets the current API user and creates an error response if user is null.
     * This method eliminates the redundant pattern of getting user and checking for null.
     * 
     * @param <T> The type of the response data
     * @param response The response object to populate with error if user is null
     * @param logger The logger to use for error logging
     * @return The API user if found, null if not found (and response is populated with error)
     */
    public static <T> User getApiUserOrSetError(GenericResponse<T> response, org.slf4j.Logger logger) {
        User apiUser = getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            response.message = "Api User is null";
            response.status = 401; // Unauthorized
        }
        return apiUser;
    }
    
    /**
     * Gets the current API user and creates an error response if user is null.
     * This method eliminates the redundant pattern of getting user and checking for null.
     * 
     * @param <T> The type of the response data
     * @param response The response object to populate with error if user is null
     * @param logger The logger to use for error logging
     * @param customErrorMessage Custom error message to use instead of default
     * @return The API user if found, null if not found (and response is populated with error)
     */
    public static <T> User getApiUserOrSetError(GenericResponse<T> response, org.slf4j.Logger logger, String customErrorMessage) {
        User apiUser = getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            response.message = customErrorMessage;
            response.status = 401; // Unauthorized
        }
        return apiUser;
    }
    
    /**
     * Gets the current API user and returns both the user and HTTP status.
     * This method is designed to work with ResponseEntity for proper HTTP status codes.
     * 
     * @param logger The logger to use for error logging
     * @return A UserResult containing the user and appropriate HTTP status
     */
    public static UserResult getApiUserWithStatus(org.slf4j.Logger logger) {
        User apiUser = getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            return new UserResult(null, HttpStatus.UNAUTHORIZED, "Api User is null");
        }
        return new UserResult(apiUser, HttpStatus.OK, null);
    }
    
    /**
     * Gets the current API user and returns both the user and HTTP status.
     * This method is designed to work with ResponseEntity for proper HTTP status codes.
     * 
     * @param logger The logger to use for error logging
     * @param customErrorMessage Custom error message to use instead of default
     * @return A UserResult containing the user and appropriate HTTP status
     */
    public static UserResult getApiUserWithStatus(org.slf4j.Logger logger, String customErrorMessage) {
        User apiUser = getApiUser();
        if (apiUser == null) {
            logger.error("Api User is null");
            return new UserResult(null, HttpStatus.UNAUTHORIZED, customErrorMessage);
        }
        return new UserResult(apiUser, HttpStatus.OK, null);
    }
    
    /**
     * Result class to hold user and HTTP status information
     */
    public static class UserResult {
        public final User user;
        public final HttpStatus httpStatus;
        public final String errorMessage;
        
        public UserResult(User user, HttpStatus httpStatus, String errorMessage) {
            this.user = user;
            this.httpStatus = httpStatus;
            this.errorMessage = errorMessage;
        }
        
        public boolean isError() {
            return user == null;
        }
    }
}
