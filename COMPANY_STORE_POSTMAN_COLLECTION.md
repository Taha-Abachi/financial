# Company and Store Management API - Postman Collection

## Overview
This Postman collection provides comprehensive testing capabilities for the Company and Store Management APIs, including CRUD operations and related gift card operations.

## Collection Structure

### 1. Authentication
- **User Login**: Authenticate and obtain JWT tokens for API access

### 2. Company Management
- **Get All Companies**: Retrieve all companies
- **Get Company by ID**: Retrieve a specific company by ID
- **Get Company by Name**: Retrieve a specific company by name
- **Create Company**: Create a new company
- **Update Company**: Update an existing company
- **Delete Company**: Delete a company
- **Check Company Exists by Name**: Verify if a company exists by name

### 3. Store Management
- **Get All Stores**: Retrieve all stores
- **Get Store by ID**: Retrieve a specific store by ID

### 4. Gift Card Company Operations
- **Get Gift Cards by Company**: Retrieve all gift cards associated with a company
- **Assign Company to Gift Card**: Link a gift card to a company
- **Remove Company from Gift Card**: Unlink a gift card from a company

### 5. Gift Card Store Operations
- **Limit Gift Card to Stores**: Restrict gift card usage to specific stores
- **Remove Store Limitation**: Remove store restrictions from a gift card

## Environment Variables

The collection uses the following variables:

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `baseUrl` | API base URL | `http://localhost:8080` |
| `accessToken` | JWT access token | (auto-populated after login) |
| `testUsername` | Test username | `admin` |
| `testPassword` | Test password | `password123` |
| `companyId` | Company ID for testing | `1` |
| `companyName` | Company name for testing | `Tech Solutions Inc` |
| `companyPhone` | Company phone for testing | `+1-555-0123` |
| `companyAddress` | Company address for testing | `123 Tech Street, Silicon Valley, CA 94000` |
| `storeId` | Store ID for testing | `1` |
| `storeIds` | Comma-separated store IDs | `1,2` |
| `giftCardSerialNo` | Gift card serial number | `GC-2024-001` |

## API Endpoints

### Company Endpoints

#### GET /api/v1/companies
- **Description**: Retrieve all companies
- **Authentication**: Required (Bearer token)
- **Response**: Array of CompanyDto objects

#### GET /api/v1/companies/{id}
- **Description**: Retrieve a specific company by ID
- **Authentication**: Required (Bearer token)
- **Parameters**: 
  - `id` (path): Company ID
- **Response**: CompanyDto object

#### GET /api/v1/companies/name/{name}
- **Description**: Retrieve a specific company by name
- **Authentication**: Required (Bearer token)
- **Parameters**: 
  - `name` (path): Company name
- **Response**: CompanyDto object

#### POST /api/v1/companies
- **Description**: Create a new company
- **Authentication**: Required (Bearer token)
- **Request Body**: Company entity with nested objects
- **Response**: CompanyDto object

#### PUT /api/v1/companies/{id}
- **Description**: Update an existing company
- **Authentication**: Required (Bearer token)
- **Parameters**: 
  - `id` (path): Company ID
- **Request Body**: Company entity with nested objects
- **Response**: CompanyDto object

#### DELETE /api/v1/companies/{id}
- **Description**: Delete a company
- **Authentication**: Required (Bearer token)
- **Parameters**: 
  - `id` (path): Company ID
- **Response**: 204 No Content

#### GET /api/v1/companies/exists/{name}
- **Description**: Check if a company exists by name
- **Authentication**: Required (Bearer token)
- **Parameters**: 
  - `name` (path): Company name
- **Response**: Boolean value

### Store Endpoints

#### GET /api/v1/store
- **Description**: Retrieve all stores
- **Authentication**: Required (Bearer token)
- **Response**: GenericResponse with array of StoreDto objects

#### GET /api/v1/store/{storeId}
- **Description**: Retrieve a specific store by ID
- **Authentication**: Required (Bearer token)
- **Parameters**: 
  - `storeId` (path): Store ID
- **Response**: GenericResponse with StoreDto object

### Gift Card Company Operations

#### GET /api/v1/giftcard/company/{companyId}
- **Description**: Get all gift cards associated with a company
- **Authentication**: Required (Bearer token)
- **Parameters**: 
  - `companyId` (path): Company ID
- **Response**: GenericResponse with array of GiftCardDto objects

#### POST /api/v1/giftcard/{serialNo}/assign-company/{companyId}
- **Description**: Assign a company to a gift card
- **Authentication**: Required (Bearer token)
- **Parameters**: 
  - `serialNo` (path): Gift card serial number
  - `companyId` (path): Company ID
- **Response**: GenericResponse with GiftCardDto object

#### POST /api/v1/giftcard/{serialNo}/remove-company
- **Description**: Remove company assignment from a gift card
- **Authentication**: Required (Bearer token)
- **Parameters**: 
  - `serialNo` (path): Gift card serial number
- **Response**: GenericResponse with null data

### Gift Card Store Operations

#### POST /api/v1/giftcard/{serialNo}/limit-stores
- **Description**: Limit a gift card to specific stores
- **Authentication**: Required (Bearer token)
- **Parameters**: 
  - `serialNo` (path): Gift card serial number
- **Request Body**: StoreLimitationRequest with storeIds array
- **Response**: GenericResponse with null data

#### POST /api/v1/giftcard/{serialNo}/remove-store-limitation
- **Description**: Remove store limitations from a gift card
- **Authentication**: Required (Bearer token)
- **Parameters**: 
  - `serialNo` (path): Gift card serial number
- **Response**: GenericResponse with null data

## Data Models

### CompanyDto
```json
{
  "id": 1,
  "companyName": "Tech Solutions Inc",
  "phoneNumber": "+1-555-0123",
  "address": "123 Tech Street, Silicon Valley, CA 94000"
}
```

### StoreDto
```json
{
  "id": 1,
  "name": "Downtown Store",
  "address": "123 Main Street, Downtown, NY 10001",
  "phone": "+1-555-0100"
}
```

### Company Entity (for requests)
```json
{
  "name": "Tech Solutions Inc",
  "phone_number": {
    "number": "+1-555-0123"
  },
  "company_address": {
    "text": "123 Tech Street, Silicon Valley, CA 94000"
  }
}
```

### StoreLimitationRequest
```json
{
  "storeIds": [1, 2, 3]
}
```

## Usage Instructions

1. **Import the Collection**: Import `CompanyAndStoreAPI_Param.postman_collection.json` into Postman
2. **Set Environment Variables**: Update the `baseUrl` variable if your API is running on a different host/port
3. **Authenticate**: Run the "User Login" request to obtain an access token
4. **Test Company Operations**: Use the Company Management folder to test CRUD operations
5. **Test Store Operations**: Use the Store Management folder to test store retrieval
6. **Test Gift Card Operations**: Use the Gift Card Company/Store Operations folders to test related functionality

## Error Responses

All endpoints may return the following error responses:

- **401 Unauthorized**: Invalid or missing authentication token
- **404 Not Found**: Resource not found
- **400 Bad Request**: Invalid request data
- **500 Internal Server Error**: Server error

## Sample Test Scenarios

### 1. Complete Company Lifecycle
1. Create a new company
2. Verify company exists by name
3. Get company by ID
4. Update company details
5. Get all companies to verify
6. Delete the company

### 2. Store Operations
1. Get all stores
2. Get a specific store by ID
3. Verify store data structure

### 3. Gift Card Company Integration
1. Create a company
2. Create/retrieve a gift card
3. Assign company to gift card
4. Get gift cards by company
5. Remove company assignment

### 4. Gift Card Store Integration
1. Get available stores
2. Limit gift card to specific stores
3. Verify limitation is applied
4. Remove store limitation

## Notes

- All requests require authentication via Bearer token
- The collection includes sample responses for successful operations
- Environment variables can be customized for different testing scenarios
- The collection follows RESTful API conventions
- Error handling is included for common scenarios
