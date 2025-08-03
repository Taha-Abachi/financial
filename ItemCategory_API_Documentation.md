# Item Category API Documentation

## Overview
The Item Category API provides endpoints for managing item categories in the financial system. Categories contain item name, description, and ID for organizing products and services.

## Base URL
```
http://localhost:8080/api/v1/item-category
```

## Authentication & Authorization
All endpoints require authentication using:
- `Authorization: Bearer {token}` header
- `x-api-key: {api-key}` header

### Access Control
- **GET operations** (`/list`, `/{categoryId}`): Accessible to both `ADMIN` and `API_USER` roles
- **POST, PUT, DELETE operations** (`/create`, `/create-bulk`, `/update/*`, `/delete/*`): Restricted to `ADMIN` role only

## Endpoints

### 1. Get All Categories
Retrieves a list of all item categories.

**Endpoint:** `GET /api/v1/item-category/list`

**Headers:**
```
Authorization: Bearer {token}
x-api-key: {api-key}
```

**Response:**
```json
{
    "status": 0,
    "message": null,
    "data": [
        {
            "id": 1,
            "name": "Electronics",
            "description": "Electronic devices and gadgets"
        },
        {
            "id": 2,
            "name": "Clothing",
            "description": "Apparel and fashion items"
        }
    ]
}
```

### 2. Get Category by ID
Retrieves a specific item category by its ID.

**Endpoint:** `GET /api/v1/item-category/{categoryId}`

**Path Parameters:**
- `categoryId` (Long): The ID of the category to retrieve

**Headers:**
```
Authorization: Bearer {token}
x-api-key: {api-key}
```

**Response:**
```json
{
    "status": 0,
    "message": null,
    "data": {
        "id": 1,
        "name": "Electronics",
        "description": "Electronic devices and gadgets"
    }
}
```

### 3. Create Single Category
Creates a new item category.

**Endpoint:** `POST /api/v1/item-category/create`

**Headers:**
```
Authorization: Bearer {token}
x-api-key: {api-key}
Content-Type: application/json
```

**Request Body:**
```json
{
    "name": "Electronics",
    "description": "Electronic devices and gadgets"
}
```

**Response:**
```json
{
    "status": 0,
    "message": null,
    "data": {
        "id": 1,
        "name": "Electronics",
        "description": "Electronic devices and gadgets"
    }
}
```

### 4. Create Multiple Categories
Creates multiple item categories in bulk.

**Endpoint:** `POST /api/v1/item-category/create-bulk`

**Headers:**
```
Authorization: Bearer {token}
x-api-key: {api-key}
Content-Type: application/json
```

**Request Body:**
```json
[
    {
        "name": "Electronics",
        "description": "Electronic devices and gadgets"
    },
    {
        "name": "Clothing",
        "description": "Apparel and fashion items"
    },
    {
        "name": "Books",
        "description": "Books and publications"
    }
]
```

**Response:**
```json
{
    "status": 0,
    "message": null,
    "data": [
        {
            "id": 1,
            "name": "Electronics",
            "description": "Electronic devices and gadgets"
        },
        {
            "id": 2,
            "name": "Clothing",
            "description": "Apparel and fashion items"
        },
        {
            "id": 3,
            "name": "Books",
            "description": "Books and publications"
        }
    ]
}
```

### 5. Update Category
Updates an existing item category.

**Endpoint:** `PUT /api/v1/item-category/update/{categoryId}`

**Path Parameters:**
- `categoryId` (Long): The ID of the category to update

**Headers:**
```
Authorization: Bearer {token}
x-api-key: {api-key}
Content-Type: application/json
```

**Request Body:**
```json
{
    "name": "Updated Electronics",
    "description": "Updated description for electronic devices and gadgets"
}
```

**Response:**
```json
{
    "status": 0,
    "message": null,
    "data": {
        "id": 1,
        "name": "Updated Electronics",
        "description": "Updated description for electronic devices and gadgets"
    }
}
```

### 6. Delete Category
Deletes an item category by ID.

**Endpoint:** `DELETE /api/v1/item-category/delete/{categoryId}`

**Path Parameters:**
- `categoryId` (Long): The ID of the category to delete

**Headers:**
```
Authorization: Bearer {token}
x-api-key: {api-key}
```

**Response:**
```json
{
    "status": 0,
    "message": null,
    "data": "Category deleted successfully"
}
```

## Error Responses

### Category Not Found
```json
{
    "status": -1,
    "message": "Item category not found",
    "data": null
}
```

### Category Already Exists
```json
{
    "status": -1,
    "message": "Category with name 'Electronics' already exists",
    "data": null
}
```

### Validation Error
```json
{
    "status": -1,
    "message": "Category ID is required for update",
    "data": null
}
```

### Authentication Error
```json
{
    "status": -1,
    "message": "Api User is null",
    "data": null
}
```

## Data Models

### ItemCategoryDto
```json
{
    "id": 1,
    "name": "Electronics",
    "description": "Electronic devices and gadgets"
}
```

**Fields:**
- `id` (Long): Unique identifier for the category
- `name` (String): Name of the category (unique)
- `description` (String): Description of the category

## Error Codes
- `-200`: Item category not found
- `-201`: Category with name already exists
- `-202`: Category ID is required for update

## Notes
- Category names must be unique
- The `id` field is auto-generated and should not be provided when creating new categories
- All endpoints require proper authentication
- Bulk operations are atomic - if any category in the list fails validation, the entire operation fails 