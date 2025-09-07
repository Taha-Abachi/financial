# Discount Code API Documentation

## Overview
The Discount Code API provides endpoints for managing discount codes, including creation, redemption, and transaction management.

## Base URL
`http://localhost:8081`

## Authentication
All API requests require an API key to be included in the request header:
```
X-API-Key: your-api-key
```

## Rate Limiting
- Maximum requests: 100 per minute
- Time window: 60 seconds
- Ban period: 5 minutes

## Common Response Format
```json
{
    "status": 0,
    "message": "Success",
    "data": {}
}
```

## Discount Code Management

### Get All Discount Codes
**Endpoint**: `GET /api/v1/discountcode/all`  
**Permissions**: ADMIN  
**Response**: List of discount codes

### Get Discount Code by Code
**Endpoint**: `GET /api/v1/discountcode/{code}`  
**Permissions**: ADMIN  
**Response**: Discount code details

### Issue Discount Code
**Endpoint**: `POST /api/v1/discountcode/issue`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "code": "string",
    "serialNo": 0,
    "discountType": "PERCENTAGE",
    "percentage": 0,
    "constantDiscountAmount": 0,
    "maxDiscountAmount": 0,
    "minimumBillAmount": 0,
    "usageLimit": 0,
    "remainingValidityPeriod": 0,
    "storeLimited": false,
    "allowedStoreIds": [1, 2, 3]
}
```
**Field Descriptions:**
- `storeLimited` (boolean, optional): If true, the discount code will only be valid at the stores specified in `allowedStoreIds`.
- `allowedStoreIds` (array of long, optional): List of store IDs where the discount code can be used. Required if `storeLimited` is true.

### Issue Multiple Discount Codes
**Endpoint**: `POST /api/v1/discountcode/issuelist`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "code": "string",
    "discountType": "PERCENTAGE",
    "percentage": 0,
    "constantDiscountAmount": 0,
    "maxDiscountAmount": 0,
    "minimumBillAmount": 0,
    "usageLimit": 0,
    "remainingValidityPeriod": 0,
    "count": 0,
    "storeLimited": false,
    "allowedStoreIds": [1, 2, 3]
}
```
**Field Descriptions:**
- `storeLimited` (boolean, optional): If true, the discount codes will only be valid at the stores specified in `allowedStoreIds`.
- `allowedStoreIds` (array of long, optional): List of store IDs where the discount codes can be used. Required if `storeLimited` is true.

### Remove Store Limitation
**Endpoint**: `POST /api/v1/discountcode/{code}/remove-store-limitation`  
**Permissions**: ADMIN

## Discount Code Transactions

### Redeem Discount Code
**Endpoint**: `POST /api/v1/discountcode/transaction/redeem`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "clientTransactionId": "string",
    "code": "string",
    "originalAmount": 0,
    "storeId": 0,
    "phoneNo": "string",
    "orderno": "string",
    "description": "string"
}
```
**Note:** If a discount code is store-limited, it can only be redeemed at the allowed stores. Attempting to redeem at a non-allowed store will result in an error (-117).

### Confirm Transaction
**Endpoint**: `POST /api/v1/discountcode/transaction/confirm`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "clientTransactionId": "string",
    "code": "string",
    "originalAmount": 0,
    "transactionId": "string",
    "orderno": "string",
    "description": "string"
}
```

### Reverse Transaction
**Endpoint**: `POST /api/v1/discountcode/transaction/reverse`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "clientTransactionId": "string",
    "code": "string",
    "originalAmount": 0,
    "transactionId": "string",
    "orderno": "string",
    "description": "string"
}
```

### Refund Transaction
**Endpoint**: `POST /api/v1/discountcode/transaction/refund`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "clientTransactionId": "string",
    "code": "string",
    "originalAmount": 0,
    "transactionId": "string",
    "orderno": "string",
    "description": "string"
}
```
**Note**: Refund can only be performed on confirmed transactions. Attempting to refund an unconfirmed or already refunded transaction will result in an error.

### Get Transaction
**Endpoint**: `GET /api/v1/discountcode/transaction/{transactionId}`  
**Permissions**: ADMIN  
**Response**: Transaction details

### List Transactions
**Endpoint**: `GET /api/v1/discountcode/transaction/list?page=0&size=10`  
**Permissions**: ADMIN  
**Query Parameters**:
- `page` (optional, default: 0): Page number (0-based)
- `size` (optional, default: 10): Number of transactions per page
**Response**: List of discount code transactions with pagination

### Check Transaction Status
**Endpoint**: `GET /api/v1/discountcode/transaction/checkStatus/{clientTransactionId}`  
**Permissions**: ADMIN  
**Response**: Transaction status

## Transaction Status Values
- PENDING: Initial state of a transaction
- CONFIRMED: Transaction has been confirmed
- REVERSED: Transaction has been reversed
- UNKNOWN: Status cannot be determined

## Discount Types
- PERCENTAGE: Discount is calculated as a percentage of the original amount
- CONSTANT: Fixed amount discount
- FREEDELIVERY: Free delivery discount

## Error Scenarios

### Discount Code Management

#### Discount Code Creation
1. Invalid Amount (-115)
   - Scenario: Attempting to create a discount code with invalid amount
   - Response: `{"status": -115, "message": "Amount must be greater than 0"}`

2. Store Not Found (-116)
   - Scenario: Attempting to limit discount code to non-existent store
   - Response: `{"status": -116, "message": "Store not found with id: {storeId}"}`

#### Discount Code Usage
1. Store Limitation (-117)
   - Scenario: Attempting to use discount code in unauthorized store
   - Response: `{"status": -117, "message": "Discount code cannot be used in this store"}`

2. Invalid Phone Number (-118)
   - Scenario: Providing invalid customer phone number format
   - Response: `{"status": -118, "message": "Customer Phone Number is incorrect"}`

3. Store Not Found (-119)
   - Scenario: Attempting transaction with non-existent store
   - Response: `{"status": -119, "message": "Store Not Found"}`

4. Discount Code Not Found (-120)
   - Scenario: Attempting to use non-existent discount code
   - Response: `{"status": -120, "message": "Discount Code not found"}`

5. Inactive Discount Code (-121)
   - Scenario: Attempting to use inactive discount code
   - Response: `{"status": -121, "message": "Discount code is inactive"}`

6. Already Used (-122)
   - Scenario: Attempting to use already used discount code
   - Response: `{"status": -122, "message": "Discount already used"}`

7. Usage Limit Reached (-123)
   - Scenario: Attempting to use discount code beyond its usage limit
   - Response: `{"status": -123, "message": "Discount code usage limit reached"}`

8. Minimum Bill Amount (-124)
   - Scenario: Original amount less than minimum bill amount
   - Response: `{"status": -124, "message": "Original amount is less than minimum bill amount required"}`

### Transaction Management

#### Transaction Validation
1. Invalid Amount (-125)
   - Scenario: Attempting transaction with zero or negative amount
   - Response: `{"status": -125, "message": "Amount must be greater than 0."}`

2. Redeem Transaction Not Found (-126)
   - Scenario: Attempting to confirm/reverse non-existent redeem transaction
   - Response: `{"status": -126, "message": "Redeem Transaction Not Found."}`

3. Amount Mismatch (-127)
   - Scenario: Confirmation/reversal amount differs from redeem amount
   - Response: `{"status": -127, "message": "Amount Not Matched."}`

4. Invalid Transaction Type (-128)
   - Scenario: Attempting invalid transaction type
   - Response: `{"status": -128, "message": "Invalid Transaction"}`

#### Transaction State
1. Already Confirmed (-129)
   - Scenario: Attempting to confirm already confirmed transaction
   - Response: `{"status": -129, "message": "Transaction already confirmed."}`

2. Already Reversed (-130)
   - Scenario: Attempting to reverse already reversed transaction
   - Response: `{"status": -130, "message": "Transaction already reversed."}`

### Customer Management
1. Customer Not Found (-102)
   - Scenario: Attempting transaction with non-existent customer
   - Response: `{"status": -102, "message": "Customer not found"}`

### Transaction ID Management
1. Duplicate Transaction ID (-101)
   - Scenario: Attempting transaction with duplicate client transaction ID
   - Response: `{"status": -101, "message": "Client Transaction Id Not Unique"}`

### General Errors
1. Validation Error (-100)
   - Scenario: General validation failure
   - Response: `{"status": -100, "message": "Validation error"}`

2. General Failure (-1)
   - Scenario: Unexpected system error
   - Response: `{"status": -1, "message": "General Failure"}`

### HTTP Status Codes
- 200: Success
- 400: Bad Request (Validation errors)
- 401: Unauthorized (Missing/invalid API key)
- 403: Forbidden (Insufficient permissions)
- 404: Not Found (Resource not found)
- 409: Conflict (Duplicate transaction ID)
- 429: Too Many Requests (Rate limit exceeded) 