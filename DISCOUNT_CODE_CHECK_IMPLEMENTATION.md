# Discount Code Check Implementation

## Overview
This implementation adds a new "Check Redeem" endpoint for discount codes that validates all redemption rules without affecting the database or creating transactions. This follows approach #1 as requested, with a separate endpoint and shared validation logic to eliminate code redundancy.

## New Endpoints

### 1. Check Discount Code
- **Endpoint**: `POST /api/v1/discountcode/transaction/check`
- **Purpose**: Validate discount code redemption rules without database changes
- **Request Body**: `DiscountCodeTransactionDto` (same as redeem endpoint)
- **Response**: `DiscountCodeTransactionDto` (same as redeem endpoint, but with `transactionId = null`)

### 2. Existing Redeem Endpoint (Refactored)
- **Endpoint**: `POST /api/v1/discountcode/transaction/redeem`
- **Purpose**: Actually redeem the discount code and create transactions
- **Changes**: Now uses shared validation logic

## Key Changes Made

### 1. **Moved Check Endpoint**
- **Before**: `POST /api/v1/discountcode/check` in `DiscountCodeController`
- **After**: `POST /api/v1/discountcode/transaction/check` in `DiscountCodeTransactionController`

### 2. **Unified Input/Output Structure**
- **Input**: Both check and redeem now use `DiscountCodeTransactionDto`
- **Output**: Both return `DiscountCodeTransactionDto`
- **Difference**: Check returns `transactionId = null`, redeem returns actual `transactionId`

### 3. **Shared Validation Logic**
- Both endpoints use the same validation method: `validateDiscountCodeRules()`
- No code duplication between check and redeem operations

## Shared Validation Logic

### Method: `validateDiscountCodeRules()`
Located in `DiscountCodeService`, this method performs all validation checks:

1. **Code Existence**: Checks if discount code exists
2. **Expiration**: Validates code hasn't expired
3. **Active Status**: Ensures code is active
4. **Usage Status**: Checks if code is already used
5. **Usage Limit**: Validates against usage count limits
6. **Minimum Bill Amount**: Ensures bill amount meets minimum requirement
7. **Store Validation**: Verifies store exists and is allowed (if store-limited)
8. **Item Category Validation**: Checks item category limitations (if applicable)
9. **Discount Calculation**: Calculates the actual discount amount

## Benefits of This Approach

### 1. **Code Reusability**
- Single validation method used by both check and redeem operations
- No duplicate validation logic
- Easier maintenance and updates

### 2. **Clear Separation of Concerns**
- Check endpoint: Validation only, no database changes
- Redeem endpoint: Validation + database operations
- Each endpoint has a single responsibility

### 3. **Better API Design**
- Follows REST principles
- Clear distinction between checking and redeeming
- Easier to implement different authorization rules

### 4. **Audit Trail**
- Clear logs for check vs. redeem operations
- Better tracking of user actions
- Easier debugging and monitoring

### 5. **Rate Limiting Flexibility**
- Different rate limits for checks (higher) vs. redeems (lower)
- Better resource management

### 6. **Consistent API Structure**
- Same input/output format for both operations
- Easier client implementation
- Better developer experience

## Usage Examples

### Check Discount Code (Validation Only)
```bash
POST /api/v1/discountcode/transaction/check
{
    "code": "SAVE20",
    "originalAmount": 5000,
    "storeId": 123,
    "phoneNo": "09123456789",
    "clientTransactionId": "TXN001",
    "orderno": "ORD123",
    "description": "Test order"
}
```

**Response** (same structure as redeem, but `transactionId = null`):
```json
{
    "status": 0,
    "message": null,
    "data": {
        "code": "SAVE20",
        "originalAmount": 5000,
        "discountAmount": 1000,
        "percentage": 20,
        "maxDiscountAmount": 5000,
        "discountType": "PERCENTAGE",
        "trxType": "Redeem",
        "status": "Pending",
        "storeId": 123,
        "phoneNo": "09123456789",
        "clientTransactionId": "TXN001",
        "storeName": "Store Name",
        "orderno": "ORD123",
        "description": "Test order",
        "transactionId": null
    }
}
```

### Redeem Discount Code (Actual Redemption)
```bash
POST /api/v1/discountcode/transaction/redeem
{
    "code": "SAVE20",
    "originalAmount": 5000,
    "storeId": 123,
    "phoneNo": "09123456789",
    "clientTransactionId": "TXN001",
    "orderno": "ORD123",
    "description": "Test order"
}
```

**Response** (same structure, but with actual `transactionId`):
```json
{
    "status": 0,
    "message": null,
    "data": {
        "code": "SAVE20",
        "originalAmount": 5000,
        "discountAmount": 1000,
        "percentage": 20,
        "maxDiscountAmount": 5000,
        "discountType": "PERCENTAGE",
        "trxType": "Redeem",
        "status": "Pending",
        "storeId": 123,
        "phoneNo": "09123456789",
        "clientTransactionId": "TXN001",
        "storeName": "Store Name",
        "orderno": "ORD123",
        "description": "Test order",
        "transactionId": "550e8400-e29b-41d4-a716-446655440000"
    }
}
```

## Error Handling

The validation method returns structured error responses with:
- `isValid`: Boolean indicating validation result
- `message`: Human-readable error message
- `errorCode`: Machine-readable error code for programmatic handling

## Testing

A comprehensive test suite has been created (`DiscountCodeServiceTest`) covering:
- Valid discount codes
- Invalid/expired codes
- Usage limit violations
- Store validation errors
- Minimum bill amount violations
- All validation scenarios

## Future Enhancements

1. **Item Category Validation**: Implement actual item category limitation logic
2. **Customer Validation**: Add customer-specific discount code rules
3. **Time-based Restrictions**: Add time-of-day or day-of-week limitations
4. **Combinatorial Rules**: Support for multiple discount codes or stacking rules
5. **Audit Logging**: Enhanced logging for compliance and debugging

## Migration Notes

- Existing redeem functionality remains unchanged from an external API perspective
- Internal implementation now uses shared validation logic
- No database schema changes required
- Backward compatible with existing clients
- Check endpoint moved to transaction controller for better organization
- Both endpoints now use consistent DTO structure
