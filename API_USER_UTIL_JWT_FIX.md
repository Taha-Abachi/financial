# ApiUserUtil JWT Authentication Fix

## Problem
The `ApiUserUtil.getApiUser()` method was only handling API key authentication (`ApiKeyAuthenticationToken`) but not JWT authentication (`UsernamePasswordAuthenticationToken`). When JWT authentication was used, the method would return `null` because it didn't recognize the authentication token type.

## Root Cause
- **API Key Authentication**: Uses `ApiKeyAuthenticationToken` which stores the `User` entity directly
- **JWT Authentication**: Uses `UsernamePasswordAuthenticationToken` with Spring Security's `UserDetails` (not the custom `User` entity)

## Solution
Updated `ApiUserUtil` to handle both authentication methods:

1. **API Key Authentication**: Extract `User` directly from `ApiKeyAuthenticationToken`
2. **JWT Authentication**: Extract username from `UserDetails` and fetch `User` entity from database using `UserRepository`

## Code Changes

### Before
```java
public static User getApiUser() {
    User apiUser = null;
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof ApiKeyAuthenticationToken token) {
        apiUser = token.getUser();
    }
    return apiUser;
}
```

### After
```java
public static User getApiUser() {
    return getApiUser(userRepository);
}

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
            String username = userDetails.getUsername();
            if (repository != null) {
                apiUser = repository.findByUsername(username).orElse(null);
            }
        }
    }
    
    return apiUser;
}
```

## Key Features
- **Backward Compatible**: Existing API key authentication continues to work
- **JWT Support**: Now properly handles JWT authentication tokens
- **Null Safety**: Handles cases where user is not found or authentication is null
- **Testable**: Added package-private method for easier unit testing

## Controllers Affected
The following controllers use `ApiUserUtil.getApiUser()` and will now work with both authentication methods:
- `DiscountCodeTransactionController`
- `GiftCardTransactionController` 
- `ItemCategoryController`

## Testing
- All existing functionality preserved
- JWT authentication now returns proper `User` entity
- Comprehensive unit tests added to verify both authentication paths
