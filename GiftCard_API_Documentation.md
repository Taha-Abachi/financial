# Gift Card API Documentation

## Overview
The Gift Card API provides endpoints for managing gift cards, including creation, transactions, and balance management.

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

## Gift Card Management

### Get All Gift Cards
**Endpoint**: `GET /api/v1/giftcard/all`  
**Permissions**: ADMIN  
**Response**: List of gift cards

### Get Gift Card by Serial Number
**Endpoint**: `GET /api/v1/giftcard/{serialNo}`  
**Permissions**: ADMIN  
**Response**: Gift card details

### Get Gift Card by Identifier
**Endpoint**: `GET /api/v1/giftcard/identifier/{identifier}`  
**Permissions**: ADMIN  
**Response**: Gift card details

### Issue Single Gift Card
**Endpoint**: `POST /api/v1/giftcard/issue`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "realAmount": 0,
    "balance": 0,
    "remainingValidityPeriod": 0
}
```
**Note:** Store limitation is not set at creation time. To limit a gift card to specific stores, use the `limit-stores` endpoint after creation.

### Issue Multiple Gift Cards
**Endpoint**: `POST /api/v1/giftcard/issuelist`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "realAmount": 0,
    "balance": 0,
    "remainingValidityPeriod": 0,
    "count": 0
}
```
**Note:** Store limitation is not set at creation time. To limit gift cards to specific stores, use the `limit-stores` endpoint after creation.

### Limit Gift Card to Stores
**Endpoint**: `POST /api/v1/giftcard/{serialNo}/limit-stores`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "storeIds": [0]
}
```

### Remove Store Limitation
**Endpoint**: `POST /api/v1/giftcard/{serialNo}/remove-store-limitation`  
**Permissions**: ADMIN

## Gift Card Transactions

### Debit Gift Card
**Endpoint**: `POST /api/v1/giftcard/transaction/debit`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "clientTransactionId": "string",
    "amount": 0,
    "giftCardSerialNo": "string",
    "storeId": 0,
    "phoneNo": "string",
    "orderno": "string",
    "description": "string"
}
```
**Note:** If a gift card is store-limited, it can only be used at the allowed stores. Attempting to use it at a non-allowed store will result in an error (-117).

### Confirm Transaction
**Endpoint**: `POST /api/v1/giftcard/transaction/confirm`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "clientTransactionId": "string",
    "amount": 0,
    "giftCardSerialNo": "string",
    "transactionId": "string",
    "orderno": "string",
    "description": "string"
}
```

### Reverse Transaction
**Endpoint**: `POST /api/v1/giftcard/transaction/reverse`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "clientTransactionId": "string",
    "amount": 0,
    "giftCardSerialNo": "string",
    "transactionId": "string",
    "orderno": "string",
    "description": "string"
}
```

### Refund Transaction
**Endpoint**: `POST /api/v1/giftcard/transaction/refund`  
**Permissions**: ADMIN  
**Request Body**:
```json
{
    "clientTransactionId": "string",
    "amount": 0,
    "giftCardSerialNo": "string",
    "transactionId": "string",
    "orderno": "string",
    "description": "string"
}
```

### Check Transaction Status
**Endpoint**: `GET /api/v1/giftcard/transaction/checkStatus/{clientTransactionId}`  
**Permissions**: ADMIN  
**Response**: Transaction status

## Transaction Status Values
- PENDING: Initial state of a transaction
- CONFIRMED: Transaction has been confirmed
- REVERSED: Transaction has been reversed
- REFUNDED: Transaction has been refunded
- UNKNOWN: Status cannot be determined

## Transaction Types
- `Credit`: Credit transaction type
- `Debit`: Debit transaction type
- `Reversal`: Transaction reversal type
- `Confirmation`: Transaction confirmation type
- `Refund`: Transaction refund type

## Error Scenarios

### Gift Card Management

#### Gift Card Creation
1. Invalid Amount (-115)
   - Scenario: Attempting to create a gift card with zero or negative real amount
   - Response: `{"status": -115, "message": "Real amount must be greater than 0"}`

2. Store Not Found (-116)
   - Scenario: Attempting to limit gift card to non-existent store
   - Response: `{"status": -116, "message": "Store not found with id: {storeId}"}`

#### Gift Card Usage
1. Store Limitation (-117)
   - Scenario: Attempting to use gift card in unauthorized store
   - Response: `{"status": -117, "message": "Gift card cannot be used in this store"}`

2. Invalid Phone Number (-118)
   - Scenario: Providing invalid customer phone number format
   - Response: `{"status": -118, "message": "Customer Phone Number is incorrect"}`

3. Store Not Found (-119)
   - Scenario: Attempting transaction with non-existent store
   - Response: `{"status": -119, "message": "Store Not Found"}`

4. Insufficient Balance (-120)
   - Scenario: Attempting debit with insufficient gift card balance
   - Response: `{"status": -120, "message": "Not enough balance"}`

5. Gift Card Not Found (-121)
   - Scenario: Attempting transaction with non-existent gift card
   - Response: `{"status": -121, "message": "General Failure"}`

### Transaction Management

#### Transaction Validation
1. Invalid Amount (-122)
   - Scenario: Attempting transaction with zero or negative amount
   - Response: `{"status": -122, "message": "Amount must be greater than 0."}`

2. Debit Transaction Not Found (-123)
   - Scenario: Attempting to confirm/reverse non-existent debit transaction
   - Response: `{"status": -123, "message": "Debit Transaction Not Found."}`

3. Amount Mismatch (-124)
   - Scenario: Confirmation/reversal amount differs from debit amount
   - Response: `{"status": -124, "message": "Debit Amount Not Matched."}`

4. Gift Card Mismatch (-125)
   - Scenario: Confirmation/reversal gift card differs from debit gift card
   - Response: `{"status": -125, "message": "Debit Gift Card Not Matched."}`

5. Invalid Transaction Type (-126)
   - Scenario: Attempting invalid transaction type (e.g., Credit, Debit, Redeem)
   - Response: `{"status": -126, "message": "Invalid Transaction"}`

#### Transaction State
1. Already Confirmed (-127)
   - Scenario: Attempting to confirm already confirmed transaction
   - Response: `{"status": -127, "message": "Transaction already confirmed."}`

2. Already Reversed (-128)
   - Scenario: Attempting to reverse already reversed transaction
   - Response: `{"status": -128, "message": "Transaction already reversed."}`

3. Already Refunded (-129)
   - Scenario: Attempting to refund already refunded transaction
   - Response: `{"status": -129, "message": "Transaction already refunded."}`

4. Already Reversed (Refund) (-130)
   - Scenario: Attempting to refund reversed transaction
   - Response: `{"status": -130, "message": "Transaction already reversed."}`

5. Not Confirmed (-131)
   - Scenario: Attempting to refund unconfirmed transaction
   - Response: `{"status": -131, "message": "Transaction not Confirmed yet."}`

6. Amount Error (-132)
   - Scenario: Invalid amount calculation in reversal/refund
   - Response: `{"status": -132, "message": "Amount Error."}`

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