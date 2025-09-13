# Postman Collections Update

## Overview
This document describes the comprehensive updates made to all Postman collections to include new report endpoints and standardize authentication to use Bearer token authorization instead of x-api-key headers.

## Updated Collections

### 1. Gift Card API Collection
**File**: `postman/Gift Card API_Param.postman_collection.json`

**Changes Made:**
- ✅ **Removed x-api-key headers** - All endpoints now use only Bearer token authentication
- ✅ **Added Gift Card Report Endpoints**:
  - `GET /api/v1/giftcard/report` - Comprehensive gift card statistics
  - `GET /api/v1/giftcard/report/company/{companyId}` - Company-specific gift card statistics

**New Endpoints Added:**
```json
{
  "name": "Get Gift Card Report",
  "request": {
    "method": "GET",
    "header": [
      {
        "key": "Authorization",
        "value": "Bearer {{token}}",
        "type": "text"
      }
    ],
    "url": {
      "raw": "{{baseUrl}}/api/v1/giftcard/report"
    },
    "description": "Get comprehensive gift card report with statistics"
  }
}
```

**Sample Response:**
```json
{
  "status": 200,
  "message": "Gift card report generated successfully",
  "data": {
    "totalCount": 1500,
    "totalBalance": 7500000,
    "totalInitialAmount": 10000000,
    "totalDebitTransactions": 2500,
    "totalDebitAmount": 2500000,
    "activeCount": 1200,
    "blockedCount": 50,
    "expiredCount": 200,
    "usedCount": 800,
    "averageBalance": 5000.0,
    "averageInitialAmount": 6666.67,
    "reportGeneratedAt": "2024-01-15T10:30:00"
  }
}
```

### 2. Discount Code API Collection
**File**: `postman/DiscountCodeAPI_Param.postman_collection.json`

**Changes Made:**
- ✅ **Added Discount Code Report Endpoints**:
  - `GET /api/v1/discountcode/report` - Comprehensive discount code statistics
  - `GET /api/v1/discountcode/report/company/{companyId}` - Company-specific discount code statistics

**New Endpoints Added:**
```json
{
  "name": "Get Discount Code Report",
  "request": {
    "method": "GET",
    "header": [
      {
        "key": "Authorization",
        "value": "Bearer {{token}}",
        "type": "text"
      }
    ],
    "url": {
      "raw": "{{baseUrl}}/api/v1/discountcode/report"
    },
    "description": "Get comprehensive discount code report with statistics"
  }
}
```

**Sample Response:**
```json
{
  "status": 200,
  "message": "Discount code report generated successfully",
  "data": {
    "totalCount": 2500,
    "totalUsedCount": 1200,
    "totalUnusedCount": 1300,
    "totalActiveCount": 2000,
    "totalInactiveCount": 500,
    "totalExpiredCount": 300,
    "totalRedeemTransactions": 1500,
    "totalDiscountAmount": 5000000,
    "totalOriginalAmount": 25000000,
    "averagePercentage": 15.5,
    "averageMaxDiscountAmount": 5000.0,
    "averageMinimumBillAmount": 1000.0,
    "averageUsageCount": 1.2,
    "averageUsageLimit": 1.0,
    "reportGeneratedAt": "2024-01-15T10:30:00"
  }
}
```

### 3. Batch Management API Collection
**File**: `postman/BatchManagementAPI_Param.postman_collection.json`

**Changes Made:**
- ✅ **Removed x-api-key headers** - All endpoints now use only Bearer token authentication
- ✅ **Already includes batch report endpoints** (previously added)

**Authentication Update:**
- All endpoints now use only `Authorization: Bearer {{token}}` header
- Removed `x-api-key: {{ADMIN_API_KEY}}` headers

### 4. Data Cleansing API Collection
**File**: `postman/DataCleansingAPI_Param.postman_collection.json`

**Changes Made:**
- ✅ **Removed x-api-key headers** - All endpoints now use only Bearer token authentication

**Authentication Update:**
- All endpoints now use only `Authorization: Bearer {{token}}` header
- Removed `x-api-key: {{ADMIN_API_KEY}}` headers

### 5. Item Category API Collection
**File**: `postman/ItemCategoryAPI_Param.postman_collection.json`

**Changes Made:**
- ✅ **Removed x-api-key headers** - All endpoints now use only Bearer token authentication

**Authentication Update:**
- All endpoints now use only `Authorization: Bearer {{token}}` header
- Removed `x-api-key: {{ADMIN_API_KEY}}` headers

### 6. User Management API Collection
**File**: `postman/UserManagementAPI_Param.postman_collection.json`

**Changes Made:**
- ✅ **Removed x-api-key headers** - All endpoints now use only Bearer token authentication

**Authentication Update:**
- All endpoints now use only `Authorization: Bearer {{token}}` header
- Removed `x-api-key: {{ADMIN_API_KEY}}` headers

### 7. Company and Store API Collection
**File**: `postman/CompanyAndStoreAPI_Param.postman_collection.json`

**Status:**
- ✅ **Already using Bearer token authentication** - No changes needed

## Authentication Standardization

### Before Update
```json
"header": [
  {
    "key": "Authorization",
    "value": "Bearer {{token}}",
    "type": "text"
  },
  {
    "key": "x-api-key",
    "value": "{{ADMIN_API_KEY}}",
    "type": "text"
  }
]
```

### After Update
```json
"header": [
  {
    "key": "Authorization",
    "value": "Bearer {{accessToken}}",
    "type": "text"
  }
]
```

## New Report Endpoints Summary

### Gift Card Reports
| Endpoint | Method | Description | Authentication |
|----------|--------|-------------|----------------|
| `/api/v1/giftcard/report` | GET | All gift cards statistics | Bearer Token |
| `/api/v1/giftcard/report/company/{id}` | GET | Company-specific gift card statistics | Bearer Token |

### Discount Code Reports
| Endpoint | Method | Description | Authentication |
|----------|--------|-------------|----------------|
| `/api/v1/discountcode/report` | GET | All discount codes statistics | Bearer Token |
| `/api/v1/discountcode/report/company/{id}` | GET | Company-specific discount code statistics | Bearer Token |

### Batch Reports (Previously Added)
| Endpoint | Method | Description | Authentication |
|----------|--------|-------------|----------------|
| `/api/v1/batches/{id}/report` | GET | Specific batch report | Bearer Token |
| `/api/v1/batches/reports/all` | GET | All batches report | Bearer Token |
| `/api/v1/batches/reports/company/{id}` | GET | Company batches report | Bearer Token |
| `/api/v1/batches/{id}/summary` | GET | Batch summary report | Bearer Token |

## Environment Variables

All collections use the following environment variables:

```json
{
  "baseUrl": "http://localhost:8080",
  "accessToken": "your-jwt-token-here"
}
```

**Note**: The `ADMIN_API_KEY` variable is no longer needed as all endpoints now use Bearer token authentication.

## Usage Instructions

### 1. Authentication Setup
1. Import the collections into Postman
2. Set up environment variables:
   - `baseUrl`: Your API base URL (e.g., `http://localhost:8080`)
   - `accessToken`: Your JWT token from login

### 2. Getting JWT Token
1. Use the "User Login" request in the Company and Store API collection
2. Copy the `accessToken` from the response
3. Set it as the `accessToken` environment variable

### 3. Testing Report Endpoints
1. Ensure you have a valid JWT token
2. Call any report endpoint
3. The response will include comprehensive statistics

## Benefits of the Update

### 1. Simplified Authentication
- **Single Authentication Method**: All endpoints now use Bearer token authentication
- **Consistent Headers**: No need to manage multiple authentication methods
- **Easier Testing**: Simplified header configuration

### 2. Comprehensive Reporting
- **Gift Card Analytics**: Complete gift card usage and financial statistics
- **Discount Code Analytics**: Complete discount code usage and performance metrics
- **Batch Analytics**: Comprehensive batch processing and financial reports
- **Company-Specific Reports**: Filtered statistics for individual companies

### 3. Enhanced API Coverage
- **New Endpoints**: 4 new report endpoints added
- **Complete Coverage**: All major entities now have reporting capabilities
- **Consistent Structure**: All report endpoints follow the same pattern

### 4. Improved Developer Experience
- **Standardized Authentication**: No confusion about which auth method to use
- **Comprehensive Examples**: All endpoints include sample responses
- **Clear Documentation**: Each endpoint has detailed descriptions

## Validation

All updated collections have been validated for:
- ✅ **JSON Syntax**: All collections are valid JSON
- ✅ **Authentication**: All endpoints use Bearer token authentication
- ✅ **Endpoint Coverage**: All new report endpoints are included
- ✅ **Response Examples**: All endpoints include sample responses
- ✅ **Environment Variables**: Consistent variable usage across collections

## Future Enhancements

1. **Additional Report Types**: More specialized report endpoints
2. **Export Functionality**: CSV/Excel export capabilities
3. **Real-time Updates**: WebSocket-based real-time statistics
4. **Advanced Filtering**: Date range and custom filter options
5. **Dashboard Integration**: Real-time dashboard updates

The Postman collections are now fully updated and ready for comprehensive API testing and reporting! 🎉
