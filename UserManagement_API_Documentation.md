# User Management API Documentation

## Overview
The User Management API provides comprehensive functionality for managing users and user roles in the financial system. This includes creating, updating, deleting, and querying users and roles with proper access control.

## Base URL
```
http://localhost:8080
```

## Authentication
All endpoints require authentication using either:
- JWT Bearer token in the `Authorization` header
- API key in the `x-api-key` header

## Access Control
- **SUPERADMIN**: Full access to all endpoints
- **ADMIN**: Full access to all endpoints
- **API_USER**: Limited access (read-only for most endpoints)
- **USER**: Basic access

## User Role Management

### Get All User Roles
**GET** `/api/v1/user-roles/list`

**Access**: ADMIN, SUPERADMIN

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "SUPERADMIN",
      "description": "Super Administrator with full system access"
    },
    {
      "id": 2,
      "name": "ADMIN",
      "description": "Administrator with management access"
    },
    {
      "id": 3,
      "name": "API_USER",
      "description": "API User with limited access"
    },
    {
      "id": 4,
      "name": "USER",
      "description": "Regular user with basic access"
    }
  ]
}
```

### Get User Role by ID
**GET** `/api/v1/user-roles/{id}`

**Access**: ADMIN, SUPERADMIN

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "SUPERADMIN",
    "description": "Super Administrator with full system access"
  }
}
```

### Get User Role by Name
**GET** `/api/v1/user-roles/name/{name}`

**Access**: ADMIN, SUPERADMIN

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "SUPERADMIN",
    "description": "Super Administrator with full system access"
  }
}
```

### Create User Role
**POST** `/api/v1/user-roles/create`

**Access**: ADMIN, SUPERADMIN

**Request Body**:
```json
{
  "name": "MANAGER",
  "description": "Manager with moderate access"
}
```

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": {
    "id": 5,
    "name": "MANAGER",
    "description": "Manager with moderate access"
  }
}
```

### Update User Role
**PUT** `/api/v1/user-roles/update/{id}`

**Access**: ADMIN, SUPERADMIN

**Request Body**:
```json
{
  "name": "MANAGER",
  "description": "Manager with enhanced access"
}
```

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": {
    "id": 5,
    "name": "MANAGER",
    "description": "Manager with enhanced access"
  }
}
```

### Delete User Role
**DELETE** `/api/v1/user-roles/delete/{id}`

**Access**: ADMIN, SUPERADMIN

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": null
}
```

## User Management

### Get All Users
**GET** `/api/v1/users/list`

**Access**: ADMIN, SUPERADMIN

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "name": "Administrator",
      "mobilePhoneNumber": "09123456789",
      "nationalCode": "1234567890",
      "email": "admin@example.com",
      "isActive": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "role": {
        "id": 2,
        "name": "ADMIN",
        "description": "Administrator with management access"
      }
    }
  ]
}
```

### Get User by ID
**GET** `/api/v1/users/{id}`

**Access**: ADMIN, SUPERADMIN

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "Administrator",
    "mobilePhoneNumber": "09123456789",
    "nationalCode": "1234567890",
    "email": "admin@example.com",
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00",
    "role": {
      "id": 2,
      "name": "ADMIN",
      "description": "Administrator with management access"
    }
  }
}
```

### Get User by Username
**GET** `/api/v1/users/username/{username}`

**Access**: ADMIN, SUPERADMIN

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "admin",
    "name": "Administrator",
    "mobilePhoneNumber": "09123456789",
    "nationalCode": "1234567890",
    "email": "admin@example.com",
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00",
    "role": {
      "id": 2,
      "name": "ADMIN",
      "description": "Administrator with management access"
    }
  }
}
```

### Create User
**POST** `/api/v1/users/create`

**Access**: ADMIN, SUPERADMIN

**Request Body**:
```json
{
  "username": "john_doe",
  "name": "John Doe",
  "password": "password123",
  "mobilePhoneNumber": "09123456788",
  "nationalCode": "1234567891",
  "email": "john@example.com",
  "roleId": 3
}
```

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": {
    "id": 2,
    "username": "john_doe",
    "name": "John Doe",
    "mobilePhoneNumber": "09123456788",
    "nationalCode": "1234567891",
    "email": "john@example.com",
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00",
    "role": {
      "id": 3,
      "name": "API_USER",
      "description": "API User with limited access"
    }
  }
}
```

### Update User
**PUT** `/api/v1/users/update/{id}`

**Access**: ADMIN, SUPERADMIN

**Request Body** (all fields optional):
```json
{
  "name": "John Smith",
  "password": "newpassword123",
  "mobilePhoneNumber": "09123456787",
  "nationalCode": "1234567892",
  "email": "john.smith@example.com",
  "roleId": 4,
  "isActive": true
}
```

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": {
    "id": 2,
    "username": "john_doe",
    "name": "John Smith",
    "mobilePhoneNumber": "09123456787",
    "nationalCode": "1234567892",
    "email": "john.smith@example.com",
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00",
    "role": {
      "id": 4,
      "name": "USER",
      "description": "Regular user with basic access"
    }
  }
}
```

### Activate User
**POST** `/api/v1/users/activate/{id}`

**Access**: ADMIN, SUPERADMIN

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": {
    "id": 2,
    "username": "john_doe",
    "name": "John Smith",
    "mobilePhoneNumber": "09123456787",
    "nationalCode": "1234567892",
    "email": "john.smith@example.com",
    "isActive": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00",
    "role": {
      "id": 4,
      "name": "USER",
      "description": "Regular user with basic access"
    }
  }
}
```

### Deactivate User
**POST** `/api/v1/users/deactivate/{id}`

**Access**: ADMIN, SUPERADMIN

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": {
    "id": 2,
    "username": "john_doe",
    "name": "John Smith",
    "mobilePhoneNumber": "09123456787",
    "nationalCode": "1234567892",
    "email": "john.smith@example.com",
    "isActive": false,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00",
    "role": {
      "id": 4,
      "name": "USER",
      "description": "Regular user with basic access"
    }
  }
}
```

### Delete User
**DELETE** `/api/v1/users/delete/{id}`

**Access**: ADMIN, SUPERADMIN

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": null
}
```

### Get Users by Role
**GET** `/api/v1/users/role/{roleId}`

**Access**: ADMIN, SUPERADMIN

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": [
    {
      "id": 2,
      "username": "john_doe",
      "name": "John Smith",
      "mobilePhoneNumber": "09123456787",
      "nationalCode": "1234567892",
      "email": "john.smith@example.com",
      "isActive": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "role": {
        "id": 3,
        "name": "API_USER",
        "description": "API User with limited access"
      }
    }
  ]
}
```

### Get Active Users
**GET** `/api/v1/users/active`

**Access**: ADMIN, SUPERADMIN

**Response**:
```json
{
  "status": 0,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "username": "admin",
      "name": "Administrator",
      "mobilePhoneNumber": "09123456789",
      "nationalCode": "1234567890",
      "email": "admin@example.com",
      "isActive": true,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:00:00",
      "role": {
        "id": 2,
        "name": "ADMIN",
        "description": "Administrator with management access"
      }
    }
  ]
}
```

## Data Models

### UserRoleDto
```json
{
  "id": 1,
  "name": "SUPERADMIN",
  "description": "Super Administrator with full system access"
}
```

### UserDto
```json
{
  "id": 1,
  "username": "admin",
  "name": "Administrator",
  "mobilePhoneNumber": "09123456789",
  "nationalCode": "1234567890",
  "email": "admin@example.com",
  "isActive": true,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00",
  "role": {
    "id": 2,
    "name": "ADMIN",
    "description": "Administrator with management access"
  }
}
```

### UserCreateRequest
```json
{
  "username": "john_doe",
  "name": "John Doe",
  "password": "password123",
  "mobilePhoneNumber": "09123456788",
  "nationalCode": "1234567891",
  "email": "john@example.com",
  "roleId": 3
}
```

### UserUpdateRequest
```json
{
  "name": "John Smith",
  "password": "newpassword123",
  "mobilePhoneNumber": "09123456787",
  "nationalCode": "1234567892",
  "email": "john.smith@example.com",
  "roleId": 4,
  "isActive": true
}
```

## Error Responses

### Validation Error
```json
{
  "status": -1,
  "message": "Username already exists",
  "data": null
}
```

### Not Found Error
```json
{
  "status": -1,
  "message": "User not found",
  "data": null
}
```

## Error Codes

| Code | Description |
|------|-------------|
| -119 | User role not found |
| -120 | User role with this name already exists |
| -121 | User not found |
| -122 | Username already exists |
| -123 | Mobile phone number already exists |
| -124 | National code already exists |
| -125 | Email already exists |

## Security Features

1. **Password Encryption**: All passwords are encrypted using BCrypt
2. **Unique Constraints**: Username, mobile phone number, national code, and email must be unique
3. **Access Control**: Role-based access control for all endpoints
4. **Input Validation**: Comprehensive validation for all input fields
5. **Audit Trail**: Created and updated timestamps for all entities

## Default Roles

The system comes with four default roles:

1. **SUPERADMIN**: Super Administrator with full system access
2. **ADMIN**: Administrator with management access
3. **API_USER**: API User with limited access
4. **USER**: Regular user with basic access

## Field Validation

### User Fields
- **username**: 3-50 characters, unique
- **name**: 2-100 characters
- **password**: Minimum 6 characters
- **mobilePhoneNumber**: 10-15 characters, unique
- **nationalCode**: 8-20 characters, unique
- **email**: Valid email format, optional, unique if provided

### UserRole Fields
- **name**: 1-50 characters, unique
- **description**: Optional text field 