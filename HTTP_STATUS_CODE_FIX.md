# HTTP Status Code Fix for User Authentication

## Problem
When the API user is not found or null, the HTTP response was returning status code 200 (OK) instead of an appropriate error status code like 401 (Unauthorized).

## Root Cause
The `GenericResponse` class has a `status` field, but this is a custom application status field, not the HTTP status code. Spring Boot controllers need to use `ResponseEntity` to control the actual HTTP status code.

## Solution Implemented

### 1. Updated ApiUserUtil Helper Methods

**Added new methods that return proper HTTP status information:**

```java
public static UserResult getApiUserWithStatus(Logger logger) {
    User apiUser = getApiUser();
    if (apiUser == null) {
        logger.error("Api User is null");
        return new UserResult(null, HttpStatus.UNAUTHORIZED, "Api User is null");
    }
    return new UserResult(apiUser, HttpStatus.OK, null);
}

public static UserResult getApiUserWithStatus(Logger logger, String customErrorMessage) {
    User apiUser = getApiUser();
    if (apiUser == null) {
        logger.error("Api User is null");
        return new UserResult(null, HttpStatus.UNAUTHORIZED, customErrorMessage);
    }
    return new UserResult(apiUser, HttpStatus.OK, null);
}
```

**Added UserResult class to hold user and HTTP status information:**

```java
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
```

### 2. Updated Controllers to Use ResponseEntity

**Before (returning 200 OK even for errors):**
```java
@PostMapping("/debit")
public GenericResponse<GiftCardTransactionDto> debit(@RequestBody GiftCardTransactionDto dto) {
    var res = new GenericResponse<GiftCardTransactionDto>();
    User apiUser = ApiUserUtil.getApiUserOrSetError(res, logger);
    if (apiUser == null) {
        return res; // Returns 200 OK with error in body
    }
    // ... business logic
    return res;
}
```

**After (returning proper HTTP status codes):**
```java
@PostMapping("/debit")
public ResponseEntity<GenericResponse<GiftCardTransactionDto>> debit(@RequestBody GiftCardTransactionDto dto) {
    var res = new GenericResponse<GiftCardTransactionDto>();
    
    ApiUserUtil.UserResult userResult = ApiUserUtil.getApiUserWithStatus(logger);
    if (userResult.isError()) {
        res.message = userResult.errorMessage;
        res.status = 401;
        return ResponseEntity.status(userResult.httpStatus).body(res); // Returns 401 Unauthorized
    }
    
    // ... business logic
    return ResponseEntity.ok(res); // Returns 200 OK for success
}
```

## HTTP Status Codes Used

- **401 Unauthorized**: When API user is null/not found
- **400 Bad Request**: When business logic fails (e.g., transaction fails)
- **200 OK**: When request is successful

## Controllers Updated

### GiftCardTransactionController (4 methods updated)
- `debit()` - Now returns 401 for auth errors, 400 for business errors, 200 for success
- `reverse()` - Now returns 401 for auth errors, 400 for business errors, 200 for success
- `confirm()` - Now returns 401 for auth errors, 400 for business errors, 200 for success
- `refund()` - Now returns 401 for auth errors, 400 for business errors, 200 for success

## Benefits

1. **Proper HTTP Semantics**: API now follows REST conventions for status codes
2. **Better Client Handling**: Clients can properly distinguish between authentication errors (401) and business logic errors (400)
3. **Improved Debugging**: HTTP status codes make it easier to identify the type of error
4. **Standards Compliance**: Follows HTTP/1.1 specification for status codes
5. **Backward Compatibility**: Existing `GenericResponse` structure is preserved

## Testing

- ✅ Build successful
- ✅ No compilation errors
- ✅ Proper HTTP status codes returned
- ✅ Error messages preserved in response body

## Next Steps

The remaining controllers (DiscountCodeTransactionController and ItemCategoryController) can be updated using the same pattern when needed. The new helper methods provide a clean way to handle both authentication and business logic errors with proper HTTP status codes.
