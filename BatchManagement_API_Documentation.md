# Batch Management API Documentation

## Overview
The Batch Management API provides functionality to create and manage batch requests for generating multiple discount codes or gift cards. Each batch consists of multiple individual requests and tracks the processing status.

## Base URL
```
http://localhost:8080
```

## Authentication & Authorization
- **Required**: API Key in `x-api-key` header
- **Required**: JWT Token in `Authorization` header (Bearer token)
- **Access Control**: All endpoints require ADMIN or SUPERADMIN role

## Endpoints

### 1. Get All Batches
**GET** `/api/v1/batches/list`

Returns all batch requests in the system.

**Headers:**
```
Authorization: Bearer <jwt-token>
x-api-key: <api-key>
```

**Response:**
```json
{
  "status": 0,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "batchNumber": "BATCH12345678",
      "batchType": "DISCOUNT_CODE",
      "description": "Holiday promotion discount codes",
      "requestDate": "2024-01-01T10:00:00",
      "totalCount": 100,
      "status": "COMPLETED",
      "processedCount": 100,
      "failedCount": 0,
      "createdAt": "2024-01-01T10:00:00",
      "updatedAt": "2024-01-01T10:05:00",
      "requestUser": {
        "id": 1,
        "name": "Administrator"
      },
      "company": {
        "id": 1,
        "name": "Test Company"
      }
    }
  ]
}
```

### 2. Get Batch by ID
**GET** `/api/v1/batches/{id}`

Returns a specific batch by its ID.

**Path Parameters:**
- `id` (Long): Batch ID

**Response:** Same structure as above but with single batch object.

### 3. Get Batch by Batch Number
**GET** `/api/v1/batches/batch-number/{batchNumber}`

Returns a specific batch by its batch number.

**Path Parameters:**
- `batchNumber` (String): Unique batch number

### 4. Create Batch
**POST** `/api/v1/batches/create`

Creates a new batch request for generating discount codes or gift cards.

**Request Body:**
```json
{
  "batchType": "DISCOUNT_CODE",
  "description": "Holiday promotion discount codes",
  "totalCount": 100,
  "companyId": 1,
  "requestUserId": 1,
  "discountCodeRequests": [
    {
      "companyId": 1,
      "balance": 1000,
      "remainingValidityPeriod": 30,
      "count": 50,
      "storeLimited": false,
      "allowedStoreIds": [],
      "itemCategoryLimited": false,
      "allowedItemCategoryIds": []
    },
    {
      "companyId": 1,
      "balance": 2000,
      "remainingValidityPeriod": 60,
      "count": 50,
      "storeLimited": true,
      "allowedStoreIds": [1, 2],
      "itemCategoryLimited": false,
      "allowedItemCategoryIds": []
    }
  ]
}
```

**For Gift Card Batch:**
```json
{
  "batchType": "GIFT_CARD",
  "description": "Corporate gift cards for employees",
  "totalCount": 50,
  "companyId": 1,
  "requestUserId": 1,
  "giftCardRequests": [
    {
      "companyId": 1,
      "balance": 5000,
      "remainingValidityPeriod": 90,
      "count": 25,
      "storeLimited": false,
      "allowedStoreIds": [],
      "itemCategoryLimited": false,
      "allowedItemCategoryIds": []
    }
  ]
}
```

### 5. Cancel Batch
**POST** `/api/v1/batches/{id}/cancel`

Cancels a batch request (only if status is PENDING or PROCESSING).

**Path Parameters:**
- `id` (Long): Batch ID

### 6. Get Batches by Type
**GET** `/api/v1/batches/type/{batchType}`

Returns all batches of a specific type.

**Path Parameters:**
- `batchType` (String): Either "DISCOUNT_CODE" or "GIFT_CARD"

### 7. Get Batches by Status
**GET** `/api/v1/batches/status/{status}`

Returns all batches with a specific status.

**Path Parameters:**
- `status` (String): One of "PENDING", "PROCESSING", "COMPLETED", "FAILED", "CANCELLED"

### 8. Get Batches by Company
**GET** `/api/v1/batches/company/{companyId}`

Returns all batches for a specific company.

**Path Parameters:**
- `companyId` (Long): Company ID

### 9. Get Batches by User
**GET** `/api/v1/batches/user/{userId}`

Returns all batches created by a specific user.

**Path Parameters:**
- `userId` (Long): User ID

## Data Models

### BatchDto
```json
{
  "id": 1,
  "batchNumber": "BATCH12345678",
  "batchType": "DISCOUNT_CODE",
  "description": "Holiday promotion discount codes",
  "requestDate": "2024-01-01T10:00:00",
  "totalCount": 100,
  "status": "COMPLETED",
  "processedCount": 100,
  "failedCount": 0,
  "errorMessage": null,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:05:00",
  "requestUser": {
    "id": 1,
    "username": "admin",
    "name": "Administrator"
  },
  "company": {
    "id": 1,
    "name": "Test Company"
  }
}
```

**Field Descriptions:**
- `id`: Unique identifier for the batch
- `batchNumber`: Unique batch number (format: BATCH + 8 random characters)
- `batchType`: Type of batch (DISCOUNT_CODE or GIFT_CARD)
- `description`: Description of the batch
- `requestDate`: Date when the batch was requested
- `totalCount`: Total number of items to be processed
- `status`: Current status of the batch (PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED)
- `processedCount`: Number of items successfully processed
- `failedCount`: Number of items that failed to process
- `errorMessage`: Detailed error message when batch fails or has failed items (null if no errors)
- `createdAt`: Date when the batch was created
- `updatedAt`: Date when the batch was last updated
- `requestUser`: User who requested the batch (contains only id and name)
- `company`: Company associated with the batch (contains only id and name)

### BatchCreateRequest
```json
{
  "batchType": "DISCOUNT_CODE",
  "description": "Holiday promotion discount codes",
  "totalCount": 100,
  "companyId": 1,
  "requestUserId": 1,
  "discountCodeRequests": [],
  "giftCardRequests": []
}
```

## Batch Status Values
- **PENDING**: Batch is created but not yet processed
- **PROCESSING**: Batch is currently being processed
- **COMPLETED**: Batch processing completed successfully
- **FAILED**: Batch processing failed
- **CANCELLED**: Batch was cancelled by user

## Batch Types
- **DISCOUNT_CODE**: Batch for generating discount codes
- **GIFT_CARD**: Batch for generating gift cards

## Error Responses

### Validation Error
```json
{
  "status": -1,
  "message": "Validation failed",
  "data": null
}
```

### Not Found Error
```json
{
  "status": -126,
  "message": "Batch not found",
  "data": null
}
```

### Cannot Cancel Error
```json
{
  "status": -127,
  "message": "Cannot cancel batch with current status",
  "data": null
}
```

## Error Codes
- `-126`: Batch not found
- `-127`: Cannot cancel batch with current status
- `-134`: Company not found
- `-121`: User not found

## Error Handling and Failure Scenarios

### Batch Failure Indication
When a batch fails or has failed items, the response will include detailed error information:

**Failed Batch Example:**
```json
{
  "id": 2,
  "batchNumber": "BATCH87654321",
  "batchType": "DISCOUNT_CODE",
  "description": "Test batch with errors",
  "requestDate": "2024-01-01T10:00:00",
  "totalCount": 10,
  "status": "FAILED",
  "processedCount": 0,
  "failedCount": 10,
  "errorMessage": "Batch processing failed: Database connection error",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:01:00",
  "requestUser": {
    "id": 1,
    "name": "Administrator"
  },
  "company": {
    "id": 1,
    "name": "Test Company"
  }
}
```

**Partial Failure Example:**
```json
{
  "id": 3,
  "batchNumber": "BATCH11111111",
  "batchType": "GIFT_CARD",
  "description": "Test batch with partial failures",
  "requestDate": "2024-01-01T10:00:00",
  "totalCount": 5,
  "status": "COMPLETED",
  "processedCount": 3,
  "failedCount": 2,
  "errorMessage": "Item 2: Invalid balance amount; Item 4: Company not found",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:02:00",
  "requestUser": {
    "id": 1,
    "name": "Administrator"
  },
  "company": {
    "id": 1,
    "name": "Test Company"
  }
}
```

### Error Message Format
- **Complete Batch Failure**: `"Batch processing failed: [specific error message]"`
- **Partial Item Failures**: `"Item [number]: [error message]; Item [number]: [error message]"`
- **No Errors**: `null` or omitted from response

### Common Failure Scenarios
1. **Database Connection Issues**: Batch processing fails due to database connectivity problems
2. **Invalid Request Parameters**: Individual items fail due to invalid input data
3. **Resource Limitations**: Processing fails due to memory or system resource constraints
4. **Business Logic Violations**: Items fail due to business rule violations (e.g., invalid company ID)
5. **External Service Failures**: Processing fails due to dependency service issues

## Features

### Asynchronous Processing
- Batches are processed asynchronously in the background
- Status is updated in real-time as processing progresses
- Failed items are tracked separately from successful ones

### Batch Number Generation
- Each batch gets a unique batch number (format: BATCH + 8 random characters)
- Batch numbers are automatically generated and guaranteed to be unique

### Flexible Request Structure
- Can specify individual requests with different parameters
- If no specific requests provided, generates default items
- Supports both discount codes and gift cards in the same system

### Status Tracking
- Real-time status updates during processing
- Separate counters for processed and failed items
- Ability to cancel batches before completion

### Filtering and Search
- Filter by batch type (DISCOUNT_CODE/GIFT_CARD)
- Filter by status (PENDING/PROCESSING/COMPLETED/FAILED/CANCELLED)
- Filter by company or user
- Get batches by batch number for quick lookup

## Security Features
- All endpoints require ADMIN or SUPERADMIN role
- API key authentication required
- JWT token validation
- Input validation and sanitization
- SQL injection protection through JPA

## Usage Examples

### Creating a Discount Code Batch
```bash
curl -X POST http://localhost:8080/api/v1/batches/create \
  -H "Authorization: Bearer <jwt-token>" \
  -H "x-api-key: <api-key>" \
  -H "Content-Type: application/json" \
  -d '{
    "batchType": "DISCOUNT_CODE",
    "description": "Holiday promotion discount codes",
    "totalCount": 100,
    "companyId": 1,
    "requestUserId": 1,
    "discountCodeRequests": [
      {
        "companyId": 1,
        "balance": 1000,
        "remainingValidityPeriod": 30,
        "count": 50
      }
    ]
  }'
```

### Creating a Gift Card Batch
```bash
curl -X POST http://localhost:8080/api/v1/batches/create \
  -H "Authorization: Bearer <jwt-token>" \
  -H "x-api-key: <api-key>" \
  -H "Content-Type: application/json" \
  -d '{
    "batchType": "GIFT_CARD",
    "description": "Corporate gift cards for employees",
    "totalCount": 50,
    "companyId": 1,
    "requestUserId": 1,
    "giftCardRequests": [
      {
        "companyId": 1,
        "balance": 5000,
        "remainingValidityPeriod": 90,
        "count": 25
      }
    ]
  }'
```

### Monitoring Batch Status
```bash
curl -X GET http://localhost:8080/api/v1/batches/1 \
  -H "Authorization: Bearer <jwt-token>" \
  -H "x-api-key: <api-key>"
``` 