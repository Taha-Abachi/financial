# Admin Access Control Implementation

## Overview

This document describes how admin access control has been implemented for the data cleansing endpoints and other administrative operations in the Financial application.

## Security Architecture

### 1. Role-Based Access Control (RBAC)

The application uses a hierarchical role-based access control system with the following roles:

- **SUPERADMIN**: Highest level access, can perform all operations
- **ADMIN**: Administrative access, can perform most operations including data cleansing
- **API_USER**: Limited access for API operations
- **USER**: Basic user access

### 2. Authentication Methods

The system supports dual authentication:

1. **JWT Authentication**: For user login sessions
2. **API Key Authentication**: For programmatic access with role-based permissions

### 3. Admin Endpoint Protection

All admin endpoints are protected under the `/api/v1/admin/**` path and require either `ADMIN` or `SUPERADMIN` role.

## Implementation Details

### Security Configuration

The `SecurityConfig.java` has been updated to include admin endpoint protection:

```java
// Admin endpoints - restricted to ADMIN and SUPERADMIN only
.requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")
```

### API Key Authentication

The `ApiKeyAuthFilter` automatically assigns appropriate roles based on the user's role in the database:

```java
AuthorityUtils.createAuthorityList(
    user.getRole().getName().equals(UserRole.API_USER.name()) 
        ? "ROLE_API_USER" 
        : "ROLE_ADMIN"
)
```

### Data Cleansing Endpoints

The following endpoints are now protected and require admin access:

- **GET** `/api/v1/admin/data-cleansing/report` - Generate inconsistency report
- **POST** `/api/v1/admin/data-cleansing/cleanse-giftcard-transactions` - Perform data cleansing
- **GET** `/api/v1/admin/data-cleansing/health-report` - Generate comprehensive health report

## Access Control Flow

### 1. Request Processing

1. Client sends request with `X-API-KEY` header
2. `ApiKeyAuthFilter` intercepts the request
3. API key is validated against the database
4. User role is determined and appropriate Spring Security role is assigned
5. Request proceeds to endpoint if role matches security requirements

### 2. Role Validation

- **ADMIN/SUPERADMIN**: Full access to all admin endpoints
- **API_USER**: Access to business logic endpoints but not admin endpoints
- **USER**: Basic access only
- **Unauthenticated**: Limited access to public endpoints

## Usage Examples

### API Key Authentication

```bash
curl -X GET "http://localhost:8080/api/v1/admin/data-cleansing/report" \
  -H "X-API-KEY: your-admin-api-key" \
  -H "Content-Type: application/json"
```

### JWT Authentication

```bash
curl -X GET "http://localhost:8080/api/v1/admin/data-cleansing/report" \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json"
```

## Security Features

### 1. Role Hierarchy

- SUPERADMIN > ADMIN > API_USER > USER
- Higher roles inherit permissions from lower roles
- Admin endpoints require explicit ADMIN or SUPERADMIN role

### 2. API Key Security

- API keys are encrypted in the database
- Keys are validated on each request
- Inactive users or users without API key permissions are blocked

### 3. Endpoint Protection

- All admin endpoints are protected by Spring Security
- Unauthorized access results in HTTP 403 Forbidden
- Role validation happens before business logic execution

## Testing Admin Access

### 1. Valid Admin Access

```bash
# Using admin API key
curl -X POST "http://localhost:8080/api/v1/admin/data-cleansing/cleanse-giftcard-transactions" \
  -H "X-API-KEY: admin-api-key" \
  -H "Content-Type: application/json"
```

### 2. Invalid Access (Should Fail)

```bash
# Using API_USER role (should fail)
curl -X POST "http://localhost:8080/api/v1/admin/data-cleansing/cleanse-giftcard-transactions" \
  -H "X-API-KEY: api-user-key" \
  -H "Content-Type: application/json"
# Expected: HTTP 403 Forbidden
```

### 3. No Authentication (Should Fail)

```bash
# No authentication header (should fail)
curl -X POST "http://localhost:8080/api/v1/admin/data-cleansing/cleanse-giftcard-transactions" \
  -H "Content-Type: application/json"
# Expected: HTTP 403 Forbidden
```

## Configuration

### Environment Variables

No additional environment variables are required. The system uses existing:

- Database configuration for user management
- API key encryption configuration
- JWT configuration for session management

### Database Requirements

Ensure admin users exist in the database with:

- Valid API keys (encrypted)
- `ADMIN` or `SUPERADMIN` role
- Active status (`isActive = true`)
- API key usage permission (`canUseApiKey = true`)

## Monitoring and Logging

### Security Events

All admin endpoint access is logged with:

- User ID and role
- Endpoint accessed
- Timestamp
- Success/failure status

### Audit Trail

Admin operations create audit trails for:

- Data cleansing operations
- Configuration changes
- User management operations

## Best Practices

### 1. API Key Management

- Rotate API keys regularly
- Use different keys for different environments
- Monitor API key usage patterns

### 2. Role Assignment

- Follow principle of least privilege
- Regularly review user roles
- Remove admin access when no longer needed

### 3. Security Monitoring

- Monitor failed authentication attempts
- Log all admin operations
- Set up alerts for suspicious activity

## Troubleshooting

### Common Issues

1. **403 Forbidden**: Check user role and API key validity
2. **Authentication Failed**: Verify API key format and encryption
3. **Role Not Found**: Ensure user has proper role assignment

### Debug Steps

1. Check user role in database
2. Verify API key encryption/decryption
3. Review security configuration
4. Check application logs for authentication details

## Conclusion

The admin access control system provides:

- **Secure**: Role-based access control with encrypted API keys
- **Flexible**: Support for both API key and JWT authentication
- **Auditable**: Comprehensive logging of all admin operations
- **Scalable**: Easy to add new admin endpoints with consistent security

All data cleansing endpoints are now properly protected and require administrative privileges to access, ensuring data integrity and security.
