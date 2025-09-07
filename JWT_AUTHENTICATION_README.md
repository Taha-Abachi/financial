# JWT Authentication System Documentation

This document describes the JWT (JSON Web Token) authentication system implemented in the Financial Spring Boot application.

## Overview

The JWT authentication system provides secure user authentication with the following features:

- **Access Tokens**: Short-lived tokens for API access (30 minutes by default)
- **Refresh Tokens**: Long-lived tokens for token renewal (7 days by default)
- **User Claims**: Tokens include user role, login IP, user agent, and referrer information
- **Token Revocation**: Support for revoking individual tokens or all user sessions
- **Automatic Cleanup**: Scheduled cleanup of expired tokens

## Architecture

### Components

1. **JwtService**: Handles JWT token generation, validation, and claims extraction
2. **AuthenticationService**: Manages login, token refresh, and logout operations
3. **JwtAuthenticationFilter**: Spring Security filter for JWT token processing
4. **CustomUserDetailsService**: Spring Security user details service
5. **RefreshToken Entity**: Database entity for storing refresh tokens
6. **TokenCleanupService**: Scheduled service for cleaning expired tokens

### Database Schema

```sql
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    login_ip VARCHAR(45),
    user_agent TEXT,
    referrer TEXT,
    is_revoked BOOLEAN DEFAULT FALSE,
    revoked_at TIMESTAMP,
    revoke_reason VARCHAR(255),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

## Configuration

### JWT Properties

Add the following properties to `application.properties`:

```properties
# JWT Configuration
jwt.secret=your-256-bit-secret-key-here-make-it-long-and-secure-for-production-use
jwt.access-token-expiration-minutes=30
jwt.refresh-token-expiration-days=7
jwt.issuer=financial-app
jwt.audience=financial-users
```

### Security Configuration

The JWT authentication filter is integrated into the Spring Security filter chain:

```java
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
```

Authentication endpoints are configured to be publicly accessible:

```java
.requestMatchers("/api/v1/auth/**").permitAll()
```

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | User login with username/password |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | Logout user (revoke refresh token) |
| POST | `/api/v1/auth/logout-all` | Logout from all sessions |

### Request/Response Examples

#### Login Request
```json
POST /api/v1/auth/login
{
    "username": "admin",
    "password": "password123"
}
```

#### Login Response
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "tokenType": "Bearer",
    "expiresAt": "2024-01-15T10:30:00",
    "user": {
        "id": 1,
        "username": "admin",
        "name": "Administrator",
        "email": "admin@example.com",
        "mobilePhoneNumber": "1234567890",
        "nationalCode": "1234567890",
        "isActive": true,
        "role": {
            "id": 1,
            "name": "ADMIN",
            "description": "Administrator role"
        }
    },
    "loginIp": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "referrer": "https://example.com/login"
}
```

#### Refresh Token Request
```json
POST /api/v1/auth/refresh
{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### Refresh Token Response
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "660e8400-e29b-41d4-a716-446655440001",
    "tokenType": "Bearer",
    "expiresAt": "2024-01-15T11:00:00"
}
```

## Token Claims

### Access Token Claims
```json
{
    "sub": "admin",
    "iss": "financial-app",
    "aud": "financial-users",
    "iat": 1705312200,
    "exp": 1705314000,
    "userId": 1,
    "role": "ADMIN",
    "loginIp": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "referrer": "https://example.com/login",
    "loginTime": "2024-01-15T10:30:00"
}
```

### Refresh Token Claims
```json
{
    "sub": "admin",
    "iss": "financial-app",
    "aud": "financial-users",
    "iat": 1705312200,
    "exp": 1705917000,
    "userId": 1,
    "role": "ADMIN",
    "loginIp": "192.168.1.100",
    "userAgent": "Mozilla/5.0...",
    "referrer": "https://example.com/login",
    "loginTime": "2024-01-15T10:30:00",
    "tokenType": "refresh"
}
```

## Usage

### Client Authentication Flow

1. **Login**: Send username/password to `/api/v1/auth/login`
2. **Store Tokens**: Store both access and refresh tokens securely
3. **API Requests**: Include access token in Authorization header
4. **Token Refresh**: When access token expires, use refresh token to get new tokens
5. **Logout**: Revoke refresh token using `/api/v1/auth/logout`

### Example Client Implementation

```javascript
// Login
const loginResponse = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: 'password123' })
});

const { accessToken, refreshToken } = await loginResponse.json();

// API Request with token
const apiResponse = await fetch('/api/v1/some-endpoint', {
    headers: { 'Authorization': `Bearer ${accessToken}` }
});

// Refresh token when needed
const refreshResponse = await fetch('/api/v1/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
});

const { accessToken: newAccessToken } = await refreshResponse.json();
```

## Security Features

### Token Security
- **HMAC-SHA256** signing algorithm
- **Configurable expiration times**
- **Token type validation** (prevents refresh tokens from being used as access tokens)
- **Automatic token cleanup** of expired tokens

### User Claims Tracking
- **Login IP address** tracking
- **User agent** information
- **Referrer** tracking
- **Login timestamp** recording

### Session Management
- **Individual token revocation**
- **Bulk session logout**
- **Database-backed token storage**
- **Revocation reason tracking**

## Maintenance

### Token Cleanup

The system automatically cleans up expired tokens daily at 2 AM:

```java
@Scheduled(cron = "0 0 2 * * ?")
public void cleanupExpiredTokens()
```

### Monitoring

Monitor the following metrics:
- Number of active refresh tokens per user
- Token expiration patterns
- Failed authentication attempts
- Token refresh frequency

## Troubleshooting

### Common Issues

1. **Token Expired**: Use refresh token to get new access token
2. **Invalid Token**: Check token format and signature
3. **User Deactivated**: Ensure user account is active
4. **Token Revoked**: User needs to login again

### Debug Logging

Enable debug logging for JWT-related components:

```properties
logging.level.com.pars.financial.security=DEBUG
logging.level.com.pars.financial.service.JwtService=DEBUG
```

## Best Practices

### Security
- Use strong, unique JWT secrets
- Keep access token expiration short (15-30 minutes)
- Store refresh tokens securely (HttpOnly cookies recommended)
- Implement rate limiting on authentication endpoints
- Log authentication events for audit purposes

### Performance
- Use database indexes for token lookups
- Implement token caching for frequently accessed tokens
- Monitor database performance for token operations
- Consider token blacklisting for immediate revocation

### Development
- Use different JWT secrets for different environments
- Implement comprehensive unit tests for authentication logic
- Add integration tests for authentication flows
- Document API changes and breaking changes

## Migration Guide

### From API Key Authentication

1. **User Registration**: Ensure all API users have corresponding user accounts
2. **Role Mapping**: Map API user permissions to user roles
3. **Token Migration**: Generate initial refresh tokens for existing users
4. **Client Updates**: Update client applications to use JWT authentication
5. **Gradual Rollout**: Deploy with both authentication methods during transition

### Database Migration

Run the migration to create the refresh_tokens table:

```sql
-- Migration V26__create_refresh_tokens_table.sql
-- This creates the refresh_tokens table with all necessary indexes
```

## Support

For issues or questions regarding the JWT authentication system:

1. Check the application logs for detailed error messages
2. Verify JWT configuration in application.properties
3. Ensure database connectivity and table structure
4. Review security configuration and endpoint permissions
