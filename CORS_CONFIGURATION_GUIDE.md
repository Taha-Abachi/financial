# CORS Configuration Guide

## Overview
This application uses Spring profiles to configure CORS (Cross-Origin Resource Sharing) settings differently for development and production environments.

## Configuration Files

### 1. Development Profile (`application-debug.properties`)
```properties
# CORS Configuration for Development
cors.allowed.origins=*
cors.allowed.methods=GET,POST,PUT,DELETE,OPTIONS,PATCH
cors.allowed.headers=*
cors.allow.credentials=true
cors.max.age=3600
```

**Development Settings:**
- ✅ **Allows all origins** (`*`) - perfect for local development
- ✅ **Allows all HTTP methods** - full API access
- ✅ **Allows all headers** - no restrictions
- ✅ **Credentials enabled** - supports authentication

### 2. Production Profile (`application-release.properties`)
```properties
# CORS Configuration for Production
cors.allowed.origins=${CORS_ALLOWED_ORIGINS:https://yourdomain.com,https://www.yourdomain.com}
cors.allowed.methods=GET,POST,PUT,DELETE,OPTIONS,PATCH
cors.allowed.headers=*
cors.allow.credentials=true
cors.max.age=3600
```

**Production Settings:**
- 🔒 **Restricted origins** - only specified domains allowed
- ✅ **Same HTTP methods** - full API access
- ✅ **All headers allowed** - but only from allowed origins
- ✅ **Credentials enabled** - supports authentication

## Environment Variables for Production

### Required Environment Variable
```bash
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com,https://api.yourdomain.com
```

### Example Production Deployment
```bash
# Set your actual domains
export CORS_ALLOWED_ORIGINS="https://myapp.com,https://www.myapp.com,https://admin.myapp.com"

# Run with release profile
java -jar your-app.jar --spring.profiles.active=release
```

## How It Works

### 1. Profile Detection
The `CorsConfig` class automatically detects the active profile and applies the appropriate CORS settings:

```java
@Value("${cors.allowed.origins:*}")
private String allowedOrigins;
```

### 2. Development Mode
- When `cors.allowed.origins=*`, uses `addAllowedOriginPattern("*")`
- Allows requests from any origin (localhost, 127.0.0.1, etc.)

### 3. Production Mode
- When `cors.allowed.origins` contains specific domains, uses `setAllowedOrigins()`
- Only allows requests from the specified domains

## Security Benefits

### Development
- **Convenient** - no CORS issues during development
- **Flexible** - works with any frontend setup
- **Safe** - only used locally

### Production
- **Secure** - only allows requests from your domains
- **Controlled** - prevents unauthorized cross-origin requests
- **Compliant** - follows security best practices

## Testing CORS Configuration

### 1. Test Development Mode
```bash
# Start with debug profile
java -jar your-app.jar --spring.profiles.active=debug

# Test from any origin (should work)
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     http://localhost:8081/api/v1/auth/login
```

### 2. Test Production Mode
```bash
# Start with release profile
export CORS_ALLOWED_ORIGINS="https://myapp.com"
java -jar your-app.jar --spring.profiles.active=release

# Test from allowed origin (should work)
curl -H "Origin: https://myapp.com" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     http://your-api.com/api/v1/auth/login

# Test from disallowed origin (should fail)
curl -H "Origin: https://malicious-site.com" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     http://your-api.com/api/v1/auth/login
```

## Common CORS Headers Returned

### Successful Request
```
Access-Control-Allow-Origin: https://myapp.com
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

### Failed Request (Production)
```
Access-Control-Allow-Origin: null
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS,PATCH
Access-Control-Allow-Headers: *
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

## Troubleshooting

### 1. CORS Error in Development
- **Check profile**: Ensure you're using `debug` profile
- **Check properties**: Verify `cors.allowed.origins=*` in debug properties
- **Restart application**: CORS config is loaded at startup

### 2. CORS Error in Production
- **Check environment variable**: Ensure `CORS_ALLOWED_ORIGINS` is set
- **Check domain format**: Use full URLs with protocol (https://)
- **Check for typos**: Domain names are case-sensitive
- **Check subdomains**: Include all necessary subdomains

### 3. Still Getting CORS Errors?
- **Check browser console**: Look for specific error messages
- **Test with curl**: Use the testing commands above
- **Check network tab**: Verify the actual headers being sent
- **Verify profile**: Confirm the correct profile is active

## Best Practices

### 1. Development
- Use `debug` profile for local development
- Keep `cors.allowed.origins=*` for flexibility
- Test with your actual frontend URLs

### 2. Production
- Always set `CORS_ALLOWED_ORIGINS` environment variable
- Use HTTPS domains only
- Include all necessary subdomains
- Test thoroughly before deployment

### 3. Security
- Never use `*` for origins in production
- Regularly review allowed origins
- Monitor CORS-related errors in logs
- Consider using a reverse proxy for additional security

## Example Deployment Scripts

### Docker Deployment
```dockerfile
# Set CORS origins as environment variable
ENV CORS_ALLOWED_ORIGINS=https://myapp.com,https://www.myapp.com

# Run with release profile
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=release"]
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: financial-api
spec:
  template:
    spec:
      containers:
      - name: api
        image: your-api:latest
        env:
        - name: CORS_ALLOWED_ORIGINS
          value: "https://myapp.com,https://www.myapp.com"
        - name: SPRING_PROFILES_ACTIVE
          value: "release"
```

This configuration ensures your API is secure in production while remaining developer-friendly during development.
