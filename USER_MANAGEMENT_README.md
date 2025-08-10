# User Management and API Key System

This document describes the comprehensive user management system with API key functionality implemented in the Financial application.

## Overview

The system provides a complete user management solution with role-based access control and API key authentication. Users can be created, updated, deleted, and managed with different roles and permissions.

## User Roles

The system supports four predefined user roles:

1. **SUPERADMIN** - Super Administrator with full system access
2. **ADMIN** - Administrator with management access  
3. **API_USER** - API User with limited access
4. **USER** - Regular user with basic access

## API Key System

### API Key Capability
Only users with the following roles can use API keys:
- SUPERADMIN
- ADMIN  
- API_USER

### API Key Features
- **Generation**: Secure 32-character random API keys
- **Encryption**: API keys are encrypted in the database
- **Validation**: API key validation endpoints
- **Revocation**: API keys can be revoked when needed

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    mobile_phone_number VARCHAR(15) NOT NULL UNIQUE,
    national_code VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role_id BIGINT NOT NULL,
    api_key TEXT,
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES user_role(id)
);
```

### User Roles Table
```sql
CREATE TABLE user_role (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
);
```

## API Endpoints

### User Management

#### Get All Users
```
GET /api/v1/users/list
```

#### Get User by ID
```
GET /api/v1/users/{id}
```

#### Get User by Username
```
GET /api/v1/users/username/{username}
```

#### Create User
```
POST /api/v1/users/create
```
**Request Body:**
```json
{
    "username": "string",
    "name": "string", 
    "password": "string",
    "mobilePhoneNumber": "string",
    "nationalCode": "string",
    "email": "string",
    "roleId": "number"
}
```

#### Update User
```
PUT /api/v1/users/update/{id}
```
**Request Body:**
```json
{
    "name": "string",
    "password": "string",
    "mobilePhoneNumber": "string",
    "nationalCode": "string",
    "email": "string",
    "roleId": "number",
    "isActive": "boolean"
}
```

#### Delete User
```
DELETE /api/v1/users/delete/{id}
```

#### Activate User
```
POST /api/v1/users/activate/{id}
```

#### Deactivate User
```
POST /api/v1/users/deactivate/{id}
```

### API Key Management

#### Generate API Key
```
POST /api/v1/users/{id}/api-key/generate
```
*Requires user to have API key capability*

#### Revoke API Key
```
POST /api/v1/users/{id}/api-key/revoke
```

#### Validate API Key
```
POST /api/v1/users/validate-api-key
```
**Request Body:** API key string

#### Get Users with API Key Capability
```
GET /api/v1/users/api-key-capable
```

#### Get Users with Active API Keys
```
GET /api/v1/users/with-active-api-keys
```

### User Queries

#### Get Users by Role
```
GET /api/v1/users/role/{roleId}
```

#### Get Active Users
```
GET /api/v1/users/active
```

#### Get User Statistics
```
GET /api/v1/users/statistics
```
**Response:**
```json
{
    "totalUsers": "number",
    "activeUsers": "number", 
    "inactiveUsers": "number",
    "usersWithApiKeys": "number",
    "apiKeyCapableUsers": "number"
}
```

### Role Management

#### Get All Roles
```
GET /api/v1/user-roles/list
```

#### Get Role by ID
```
GET /api/v1/user-roles/{id}
```

#### Get Role by Name
```
GET /api/v1/user-roles/name/{name}
```

#### Create Role
```
POST /api/v1/user-roles/create
```
**Request Body:**
```json
{
    "name": "string",
    "description": "string"
}
```

#### Update Role
```
PUT /api/v1/user-roles/update/{id}
```

#### Delete Role
```
DELETE /api/v1/user-roles/delete/{id}
```

#### Initialize Default Roles
```
POST /api/v1/user-roles/initialize-defaults
```
*Creates the four default roles if they don't exist*

### System Initialization

#### Initialize Default Super Admin
```
POST /api/v1/users/initialize-super-admin
```
*Creates a default super admin user if none exists*

## Security Features

### Password Security
- Passwords are automatically encrypted using BCrypt
- Password validation and matching methods provided

### API Key Security
- API keys are encrypted in the database
- Secure random generation (32 characters)
- Role-based access control for API key usage

### User Validation
- Unique constraints on username, mobile phone, national code, and email
- Active/inactive user status management
- Role validation and assignment

## Error Codes

The system uses standardized error codes:

- `-119`: User role not found
- `-120`: User role with this name already exists
- `-121`: User not found
- `-122`: Username already exists
- `-123`: Mobile phone number already exists
- `-124`: National code already exists
- `-125`: Email already exists
- `-126`: User role does not support API key usage
- `-127`: Invalid API key

## Usage Examples

### Creating an API User
1. First, ensure the API_USER role exists
2. Create a user with the API_USER role
3. Generate an API key for the user

### Managing API Keys
1. Use the generate endpoint to create new API keys
2. Store the returned API key securely
3. Use the revoke endpoint to invalidate keys when needed
4. Validate API keys using the validation endpoint

### System Setup
1. Initialize default roles: `POST /api/v1/user-roles/initialize-defaults`
2. Initialize super admin: `POST /api/v1/users/initialize-super-admin`
3. Create additional users as needed

## Best Practices

1. **API Key Security**: Never expose API keys in logs or error messages
2. **Role Assignment**: Assign minimal required roles to users
3. **Password Policy**: Implement strong password requirements
4. **Regular Review**: Periodically review user access and API key usage
5. **Audit Trail**: Monitor user creation, updates, and API key operations

## Dependencies

- Spring Boot Security
- Spring Data JPA
- BCrypt for password encryption
- Custom API key encryption utility
- Flyway for database migrations

## Database Migrations

The system includes the following migrations:
- `V22__create_user_roles_table.sql` - Creates user roles table
- `V23__create_users_table.sql` - Creates users table
- `V27__add_api_key_to_users.sql` - Adds API key support

## Testing

The system can be tested using the provided Postman collections in the `postman/` directory. Ensure the application is running and the database is properly configured before testing.
