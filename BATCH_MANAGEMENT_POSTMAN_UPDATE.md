# Batch Management API Postman Collection Update

## Overview
The Batch Management API Postman collection has been updated to include comprehensive batch reporting capabilities. The collection now provides endpoints for both batch management operations and detailed reporting functionality.

## Updated Collection Structure

### Basic Batch Management Operations
- **Get All Batches** - `GET /api/v1/batches/list`
- **Get Batch by ID** - `GET /api/v1/batches/{id}`
- **Get Batch by Batch Number** - `GET /api/v1/batches/batch-number/{batchNumber}`
- **Create Discount Code Batch** - `POST /api/v1/batches/create`
- **Create Gift Card Batch** - `POST /api/v1/batches/create`
- **Cancel Batch** - `POST /api/v1/batches/{id}/cancel`
- **Get Batches by Type** - `GET /api/v1/batches/type/{batchType}`
- **Get Batches by Status** - `GET /api/v1/batches/status/{status}`
- **Get Batches by Company** - `GET /api/v1/batches/company/{companyId}`
- **Get Batches by User** - `GET /api/v1/batches/user/{userId}`
- **Get Failed Batches** - `GET /api/v1/batches/status/FAILED`

### New Batch Reporting Operations
- **Get Batch Report** - `GET /api/v1/batches/{batchId}/report`
- **Get All Batches Report** - `GET /api/v1/batches/reports/all`
- **Get Company Batches Report** - `GET /api/v1/batches/reports/company/{companyId}`
- **Get Batch Summary** - `GET /api/v1/batches/{batchId}/summary`

## Authentication & Authorization

### Batch Management Operations
- **Required Roles**: ADMIN or SUPERADMIN
- **Authentication**: JWT Bearer token or API Key
- **Headers**: 
  - `Authorization: Bearer {{token}}`
  - `x-api-key: {{ADMIN_API_KEY}}`

### Batch Reporting Operations
- **Required Roles**: ADMIN, API_USER, or SUPERADMIN
- **Authentication**: JWT Bearer token or API Key
- **Headers**: 
  - `Authorization: Bearer {{token}}`
  - `x-api-key: {{ADMIN_API_KEY}}`

## Report Response Structure

### BatchReportDto
```json
{
  "batchId": 1,
  "batchNumber": "BATCH12345678",
  "description": "Holiday promotion discount codes",
  "batchType": "DISCOUNT_CODE",
  "status": "COMPLETED",
  "totalRequested": 100,
  "totalProcessed": 100,
  "totalFailed": 0,
  "successRate": 100.0,
  "giftCardSummary": {
    "totalGenerated": 50,
    "totalUsed": 20,
    "totalRemaining": 30,
    "totalValue": 750000,
    "usedValue": 300000,
    "remainingValue": 450000,
    "averageBalance": 15000,
    "maxBalance": 10000,
    "minBalance": 5000
  },
  "discountCodeSummary": {
    "totalGenerated": 100,
    "totalUsed": 45,
    "totalRemaining": 55,
    "totalValue": 500000,
    "usedValue": 225000,
    "remainingValue": 275000,
    "averageDiscount": 10.0,
    "maxDiscountAmount": 5000,
    "minDiscountAmount": 1000
  },
  "financialSummary": {
    "totalValue": 500000,
    "usedValue": 225000,
    "remainingValue": 275000,
    "usageRate": 45.0
  },
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:05:00"
}
```

## Key Features

### 1. Comprehensive Reporting
- **Financial Metrics**: Total value, used value, remaining value, usage rate
- **Usage Statistics**: Generated, used, remaining counts
- **Success Rates**: Processing success rates and failure analysis
- **Type-Specific Data**: Separate summaries for gift cards and discount codes

### 2. Flexible Access Control
- **API_USER Role**: Can access all reporting endpoints
- **ADMIN Role**: Can access all batch management and reporting endpoints
- **SUPERADMIN Role**: Full access to all operations

### 3. Multiple Report Types
- **Individual Batch Reports**: Detailed analysis of specific batches
- **All Batches Reports**: System-wide batch analysis
- **Company-Specific Reports**: Batches filtered by company
- **Summary Reports**: Quick overview of batch performance

## Usage Examples

### Get Detailed Batch Report
```bash
GET /api/v1/batches/1/report
Authorization: Bearer your-jwt-token
x-api-key: your-api-key
```

### Get All Batches Report
```bash
GET /api/v1/batches/reports/all
Authorization: Bearer your-jwt-token
x-api-key: your-api-key
```

### Get Company Batches Report
```bash
GET /api/v1/batches/reports/company/1
Authorization: Bearer your-jwt-token
x-api-key: your-api-key
```

## Environment Variables

The collection uses the following environment variables:
- `{{baseUrl}}`: API base URL (default: http://localhost:8080)
- `{{token}}`: JWT authentication token
- `{{ADMIN_API_KEY}}`: API key for authentication

## Benefits of the Update

1. **Unified API**: All batch operations and reports in one collection
2. **Comprehensive Reporting**: Detailed financial and usage analytics
3. **Flexible Access**: Different permission levels for different user types
4. **Easy Testing**: Pre-configured requests with example responses
5. **Documentation**: Clear descriptions and response examples

## Migration Notes

- **No Breaking Changes**: All existing endpoints remain unchanged
- **Enhanced Functionality**: New reporting capabilities added
- **Same Authentication**: Uses existing authentication mechanisms
- **Consistent Response Format**: Maintains GenericResponse structure

The updated collection provides a complete solution for batch management and reporting, making it easier to monitor and analyze batch performance across the system.
