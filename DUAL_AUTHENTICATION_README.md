# Dual Authentication System Documentation

This document explains how both JWT (JSON Web Token) and API Key authentication work together in the Financial Spring Boot application.

## Overview

The application supports two authentication methods that can coexist:

1. **JWT Authentication**: For user-based authentication with username/password
2. **API Key Authentication**: For service-to-service or application-to-application communication

Both authentication methods work independently and can be used simultaneously without conflicts.

## Authentication Flow

### Filter Chain Order

The authentication filters are executed in the following order:

1. **SecurityContextFilter** - Sets up security context
2. **RateLimitFilter** - Applies rate limiting
3. **ApiKeyAuthFilter** - Processes API key authentication
4. **JwtAuthenticationFilter** - Processes JWT authentication
5. **UsernamePasswordAuthenticationFilter** - Default Spring Security filter

### Priority Logic

- **API Key Priority**: If an API key is present and valid, it takes precedence
- **JWT Fallback**: If no API key is present, JWT authentication is processed
- **No Conflicts**: Each filter checks if authentication is already established before processing

## API Key Authentication

### Usage

Include the API key in the request header:

```http
X-API-KEY: your-api-key-here
```

### Features

- **Service-to-Service**: Ideal for machine-to-machine communication
- **Long-lived**: API keys don't expire (until manually revoked)
- **Role-based**: Supports `ROLE_API_USER` and `ROLE_ADMIN` roles
- **Simple**: No token refresh needed

### Example Request

```bash
curl -X GET "http://localhost:8081/api/v1/giftcard/balance/12345678" \
  -H "X-API-KEY: your-api-key-here" \
  -H "Content-Type: application/json"
```

## JWT Authentication

### Usage

Include the JWT token in the Authorization header:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Features

- **User-based**: Requires username/password login
- **Short-lived**: Access tokens expire (30 minutes default)
- **Refreshable**: Long-lived refresh tokens for token renewal
- **Rich Claims**: Includes user role, IP, user agent, referrer
- **Session Management**: Support for logout and session revocation

### Example Request

```bash
curl -X GET "http://localhost:8081/api/v1/giftcard/balance/12345678" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json"
```

## Authentication Endpoints

### JWT Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | User login with username/password |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | Logout user (revoke refresh token) |
| POST | `/api/v1/auth/logout-all` | Logout from all sessions |

### API Key Management

API keys are managed through the existing API user management system:

- **Creation**: Through the API user management endpoints
- **Validation**: Automatic validation on each request
- **Revocation**: Through the API user management system

## Security Configuration

### Filter Configuration

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .addFilterBefore(new SecurityContextFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(new RateLimitFilter(rateLimitProperties), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(new ApiKeyAuthFilter(apiUserRepository, apiKeyEncryption), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        // ... authorization rules
}
```

### Authorization Rules

Both authentication methods use the same role-based authorization:

```java
.requestMatchers("/api/v1/giftcard/*").hasRole("API_USER")
.requestMatchers("/api/v1/admin/*").hasRole("ADMIN")
.requestMatchers("/api/v1/superadmin/*").hasRole("SUPERADMIN")
```

## Use Cases

### When to Use API Key Authentication

- **Service Integration**: When integrating with other services
- **Automated Scripts**: For automated testing or data processing
- **Long-term Access**: When you need persistent access without user interaction
- **High-frequency Requests**: For applications making many requests

### When to Use JWT Authentication

- **User Applications**: Web applications or mobile apps with user login
- **Interactive Sessions**: When users need to log in and out
- **Rich User Context**: When you need detailed user information and session tracking
- **Security-sensitive Operations**: When you need fine-grained session control

## Implementation Details

### Filter Cooperation

Both filters implement cooperation logic:

**ApiKeyAuthFilter:**
```java
// Skip API key processing if JWT authentication is already present
if (SecurityContextHolder.getContext().getAuthentication() != null && 
    SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordAuthenticationToken) {
    filterChain.doFilter(request, response);
    return;
}
```

**JwtAuthenticationFilter:**
```java
// Skip JWT processing if API key authentication is already present
if (SecurityContextHolder.getContext().getAuthentication() != null && 
    SecurityContextHolder.getContext().getAuthentication() instanceof ApiKeyAuthenticationToken) {
    filterChain.doFilter(request, response);
    return;
}
```

### Authentication Token Types

- **ApiKeyAuthenticationToken**: For API key-based authentication
- **UsernamePasswordAuthenticationToken**: For JWT-based authentication

## Testing Both Authentication Methods

### Testing API Key Authentication

```bash
# Test API key authentication
curl -X GET "http://localhost:8081/api/v1/giftcard/balance/12345678" \
  -H "X-API-KEY: your-api-key-here"

# Expected response: 200 OK with gift card balance
```

### Testing JWT Authentication

```bash
# 1. Login to get JWT token
curl -X POST "http://localhost:8081/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "password123"}'

# Expected response: 200 OK with access token and refresh token

# 2. Use JWT token for API access
curl -X GET "http://localhost:8081/api/v1/giftcard/balance/12345678" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"

# Expected response: 200 OK with gift card balance
```

### Testing Both Together

```bash
# Request with both authentication methods (API key takes precedence)
curl -X GET "http://localhost:8081/api/v1/giftcard/balance/12345678" \
  -H "X-API-KEY: your-api-key-here" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"

# Expected: API key authentication is used, JWT is ignored
```

## Error Handling

### API Key Errors

- **Missing API Key**: 401 Unauthorized
- **Invalid API Key**: 401 Unauthorized
- **Expired API Key**: 401 Unauthorized (if implemented)

### JWT Errors

- **Missing Token**: 401 Unauthorized
- **Invalid Token**: 401 Unauthorized
- **Expired Token**: 401 Unauthorized
- **Invalid Refresh Token**: 401 Unauthorized

### Common Error Responses

```json
{
    "timestamp": "2024-01-15T10:30:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Invalid authentication credentials",
    "path": "/api/v1/giftcard/balance/12345678"
}
```

## Best Practices

### Security

1. **Use HTTPS**: Always use HTTPS in production
2. **Secure Storage**: Store API keys securely (environment variables, secure vaults)
3. **Token Expiration**: Use short-lived JWT tokens (15-30 minutes)
4. **Rate Limiting**: Implement rate limiting for both authentication methods
5. **Audit Logging**: Log authentication events for security monitoring

### Performance

1. **Caching**: Cache API key validation results
2. **Database Optimization**: Use indexes for API key lookups
3. **Token Validation**: Implement efficient JWT validation
4. **Connection Pooling**: Use connection pooling for database operations

### Development

1. **Environment Separation**: Use different API keys for different environments
2. **Testing**: Test both authentication methods thoroughly
3. **Documentation**: Document API key requirements for each endpoint
4. **Monitoring**: Monitor authentication success/failure rates

## Migration Strategy

### From API Key Only

1. **Keep Existing API Keys**: Maintain existing API key functionality
2. **Add JWT Support**: Implement JWT authentication alongside API keys
3. **Gradual Migration**: Allow clients to migrate to JWT at their own pace
4. **Documentation**: Update API documentation to include both methods

### From JWT Only

1. **Add API Key Support**: Implement API key authentication
2. **Service Integration**: Create API keys for service-to-service communication
3. **User Experience**: Maintain JWT for user-facing applications
4. **Testing**: Test both authentication methods thoroughly

## Troubleshooting

### Common Issues

1. **Authentication Conflicts**: Ensure filters are in correct order
2. **Role Mismatches**: Verify API keys and JWT tokens have correct roles
3. **Token Expiration**: Check JWT token expiration times
4. **API Key Format**: Verify API key format and encryption

### Debug Logging

Enable debug logging for authentication:

```properties
logging.level.com.pars.financial.security=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Monitoring

Monitor the following metrics:

- Authentication success/failure rates by method
- Token refresh frequency
- API key usage patterns
- Authentication latency

## Support

For issues with dual authentication:

1. Check application logs for detailed error messages
2. Verify authentication configuration
3. Test both authentication methods independently
4. Review security configuration and endpoint permissions
5. Check database connectivity and API key storage
