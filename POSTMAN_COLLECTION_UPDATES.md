# Postman Collection Updates

## Overview
The Discount Code API Postman collection has been updated to include the new check endpoint and enhanced request/response examples.

## New Endpoint Added

### Check Discount Code
- **Method**: `POST`
- **URL**: `{{baseUrl}}/api/v1/discountcode/transaction/check`
- **Description**: Check discount code validity without affecting the database. Returns the same structure as redeem but with transactionId = null.

#### Request Headers
```
Authorization: Bearer {{token}}
Content-Type: application/json
x-api-key: {{API_USER_KEY}}
```

#### Request Body
```json
{
    "code": "{{discountCode}}",
    "originalAmount": 1000,
    "storeId": 1,
    "phoneNo": "1234567890",
    "clientTransactionId": "{{clienttransactionid}}",
    "orderno": "ORD123",
    "description": "Test order for validation"
}
```

#### Response Examples

**Success Response (200 OK)**
```json
{
    "status": 0,
    "message": null,
    "data": {
        "code": "ABC123",
        "originalAmount": 1000,
        "discountAmount": 200,
        "percentage": 20,
        "maxDiscountAmount": 500,
        "discountType": "PERCENTAGE",
        "trxType": "Redeem",
        "status": "Pending",
        "storeId": 1,
        "phoneNo": "1234567890",
        "clientTransactionId": "TRX123",
        "storeName": "Test Store",
        "orderno": "ORD123",
        "description": "Test order for validation",
        "transactionId": null
    }
}
```

**Validation Error Response (400 Bad Request)**
```json
{
    "status": -1,
    "message": "Discount Code not found.",
    "data": null
}
```

#### Test Scripts
The check endpoint includes comprehensive test scripts that:
1. Verify HTTP status is 200
2. Validate response structure matches redeem endpoint
3. Ensure `transactionId` is `null`
4. Check all required fields are present

## Updated Endpoints

### Redeem Discount Code
The redeem endpoint has been enhanced to include the new fields:

#### Request Body (Updated)
```json
{
    "code": "{{discountCode}}",
    "originalAmount": 1000,
    "storeId": 1,
    "phoneNo": "1234567890",
    "clientTransactionId": "{{clienttransactionid}}",
    "orderno": "ORD123",
    "description": "Test order for redemption"
}
```

#### Enhanced Test Scripts
- Added validation for new fields (`orderno`, `description`)
- Ensures `transactionId` is not null (unlike check endpoint)
- Validates response structure consistency

## Key Features

### 1. **Consistent Structure**
- Both check and redeem endpoints use identical request/response structures
- Only difference: `transactionId` is `null` for check, actual UUID for redeem

### 2. **Enhanced Validation**
- Comprehensive test scripts for both endpoints
- Ensures API consistency and reliability
- Validates all required fields are present

### 3. **Better Documentation**
- Clear descriptions for each endpoint
- Example request/response bodies
- Error response examples

### 4. **Environment Variables**
- Uses `{{discountCode}}` for dynamic testing
- Generates unique `clientTransactionId` timestamps
- Stores `transactionId` for subsequent operations

## Usage Workflow

### 1. **Check First (Validation)**
```bash
POST /api/v1/discountcode/transaction/check
```
- Validate discount code without database changes
- Get calculated discount amount
- Check all business rules

### 2. **Redeem (Actual Transaction)**
```bash
POST /api/v1/discountcode/transaction/redeem
```
- Use same request structure
- Creates actual transaction
- Returns real `transactionId`

### 3. **Confirm/Reverse/Refund**
- Use the `transactionId` from redeem for subsequent operations
- All transaction lifecycle operations remain unchanged

## Testing Scenarios

### Valid Discount Code
- Check endpoint returns success with calculated discount
- Redeem endpoint creates transaction and returns ID
- Both endpoints return identical structure (except transactionId)

### Invalid Discount Code
- Check endpoint returns validation error
- Redeem endpoint returns same validation error
- Consistent error handling across both endpoints

### Business Rule Violations
- Expired codes
- Usage limit exceeded
- Store restrictions
- Minimum amount requirements

## Environment Variables Required

```json
{
    "baseUrl": "http://localhost:8080",
    "token": "your-jwt-token",
    "API_USER_KEY": "your-api-key",
    "ADMIN_API_KEY": "admin-api-key",
    "discountCode": "auto-populated-from-issue-endpoint"
}
```

## Benefits

1. **Consistent API**: Same structure for check and redeem
2. **Better Testing**: Comprehensive validation scripts
3. **Clear Documentation**: Example requests and responses
4. **Error Handling**: Validation error examples
5. **Workflow Support**: Natural progression from check to redeem

## Migration Notes

- Existing redeem functionality remains unchanged
- New check endpoint provides validation without side effects
- Enhanced test coverage ensures API reliability
- Backward compatible with existing workflows
