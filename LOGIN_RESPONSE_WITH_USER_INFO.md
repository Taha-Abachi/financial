# Login Response with User Information

## Overview
The login service now returns user information along with authentication tokens, providing clients with non-confidential user data immediately after successful authentication.

## Updated Login Response Structure

### New LoginResponse Format
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "660e8400-e29b-41d4-a716-446655440001",
    "tokenType": "Bearer",
    "expiresAt": "2024-01-15T11:00:00",
    "loginIp": "192.168.1.100",
    "userAgent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
    "referrer": "https://example.com/login",
    "user": {
        "id": 1,
        "username": "admin",
        "name": "Administrator",
        "email": "admin@example.com",
        "mobilePhoneNumber": "+1234567890",
        "isActive": true,
        "createdAt": "2024-01-01T10:00:00",
        "updatedAt": "2024-01-15T10:30:00",
        "role": {
            "id": 1,
            "name": "ADMIN",
            "description": "Administrator role with full access"
        }
    }
}
```

## User Information Fields

### Included Fields (Non-Confidential)
| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Unique user identifier |
| `username` | String | User's login username |
| `name` | String | User's display name |
| `email` | String | User's email address |
| `mobilePhoneNumber` | String | User's mobile phone number |
| `isActive` | Boolean | Whether the user account is active |
| `createdAt` | LocalDateTime | Account creation timestamp |
| `updatedAt` | LocalDateTime | Last update timestamp |
| `role` | UserRoleInfoDto | User's role information |

### Excluded Fields (Confidential)
| Field | Reason for Exclusion |
|-------|---------------------|
| `password` | Security - never expose passwords |
| `nationalCode` | Privacy - sensitive personal information |
| `apiKey` | Security - API keys should be managed separately |

## User Role Information

### UserRoleInfoDto Structure
```json
{
    "id": 1,
    "name": "ADMIN",
    "description": "Administrator role with full access"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | Long | Role identifier |
| `name` | String | Role name (e.g., "ADMIN", "API_USER") |
| `description` | String | Human-readable role description |

## Implementation Details

### 1. UserInfoDto Class
- **Purpose**: Contains only non-confidential user data
- **Location**: `src/main/java/com/pars/financial/dto/UserInfoDto.java`
- **Features**: 
  - Nested `UserRoleInfoDto` for role information
  - All sensitive fields excluded
  - Proper getters/setters for JSON serialization

### 2. UserMapper Class
- **Purpose**: Converts User entity to UserInfoDto
- **Location**: `src/main/java/com/pars/financial/mapper/UserMapper.java`
- **Features**:
  - Filters out confidential data
  - Maps role information safely
  - Null-safe operations

### 3. Updated AuthenticationService
- **Changes**: 
  - Injects `UserMapper` dependency
  - Converts user to DTO before returning
  - Uses new constructor with user information

### 4. Updated LoginResponse
- **Changes**:
  - Added `user` field of type `UserInfoDto`
  - New constructor accepting user information
  - Backward compatible with existing constructor

## Security Considerations

### ✅ **Data Protection**
- **No passwords** in response
- **No national codes** (sensitive personal data)
- **No API keys** (security tokens)
- **Only public profile data** included

### ✅ **Role Information**
- **Safe to expose** role names and descriptions
- **No sensitive permissions** details
- **Helps frontend** make UI decisions

### ✅ **Privacy Compliance**
- **Minimal data exposure** principle
- **Only necessary information** for client functionality
- **No tracking or analytics data**

## Client Usage Examples

### JavaScript/TypeScript
```javascript
// Login request
const loginResponse = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: 'password123' })
});

const loginData = await loginResponse.json();

// Access user information
const { accessToken, refreshToken, user } = loginData;

console.log('Welcome,', user.name);
console.log('Your role:', user.role.name);
console.log('Account created:', user.createdAt);

// Store tokens and user info
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('refreshToken', refreshToken);
localStorage.setItem('userInfo', JSON.stringify(user));
```

### React Example
```jsx
const [user, setUser] = useState(null);

const handleLogin = async (credentials) => {
    const response = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(credentials)
    });
    
    const data = await response.json();
    
    // Store user information in state
    setUser(data.user);
    
    // Store tokens
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
};

// Use user information in UI
return (
    <div>
        <h1>Welcome, {user?.name}</h1>
        <p>Role: {user?.role?.name}</p>
        <p>Email: {user?.email}</p>
    </div>
);
```

## API Documentation Updates

### Swagger/OpenAPI
The login endpoint now returns the enhanced response structure with user information included.

### Postman Collection
Update your Postman collection to expect the new response format with the `user` object.

## Backward Compatibility

### ✅ **Existing Clients**
- **Old constructor** still available
- **New field is optional** in JSON
- **Gradual migration** possible

### ✅ **API Versioning**
- **Same endpoint** (`/api/v1/auth/login`)
- **Enhanced response** format
- **No breaking changes** for existing functionality

## Testing

### Unit Tests
```java
@Test
void testLoginResponseIncludesUserInfo() {
    // Given
    LoginRequest request = new LoginRequest("admin", "password");
    
    // When
    LoginResponse response = authenticationService.login(request, "127.0.0.1", "TestAgent", null);
    
    // Then
    assertNotNull(response.getUser());
    assertEquals("admin", response.getUser().getUsername());
    assertEquals("Administrator", response.getUser().getName());
    assertEquals("ADMIN", response.getUser().getRole().getName());
    assertNull(response.getUser().getPassword()); // Should not include password
}
```

### Integration Tests
```java
@Test
void testLoginEndpointReturnsUserInfo() {
    // Given
    String loginJson = "{\"username\":\"admin\",\"password\":\"password123\"}";
    
    // When
    ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
        "/api/v1/auth/login", 
        loginJson, 
        LoginResponse.class
    );
    
    // Then
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody().getUser());
    assertEquals("admin", response.getBody().getUser().getUsername());
}
```

## Benefits

### 🚀 **Improved User Experience**
- **Immediate user context** after login
- **No additional API calls** needed for basic user info
- **Faster UI rendering** with user data

### 🔧 **Developer Experience**
- **Single API call** for authentication + user data
- **Consistent data structure** across the application
- **Easy frontend state management**

### 🛡️ **Security**
- **No sensitive data** exposed
- **Minimal information** principle
- **Safe for client-side storage**

This enhancement provides a better developer and user experience while maintaining security best practices by only exposing non-confidential user information in the login response.
