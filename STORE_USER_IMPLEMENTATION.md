# Store User Implementation

This document describes the implementation of the STORE_USER role and associated functionality for store-specific transaction management.

## Overview

The STORE_USER role provides store-specific access to gift card and discount code transactions, allowing store users to view and search transactions only for their associated store. This includes time-based transaction summaries and filtered search capabilities.

## Features Implemented

### 1. STORE_USER Role
- Added `STORE_USER` to the `UserRole` enum
- Updated database migration to include the new role
- Modified `UserRoleService` to initialize the STORE_USER role
- Updated `User` entity to include store association

### 2. User-Store Association
- Added `store_id` column to the `users` table
- Created foreign key constraint linking users to stores
- Added `Store` field to the `User` entity with lazy loading
- Updated `User.canUseApiKey()` to include STORE_USER role

### 3. Store-Specific Transaction Access
- Created `StoreUserService` for store-specific operations
- Added repository methods for store-filtered queries:
  - `findByStoreId(Long storeId)` for both gift card and discount code transactions
  - `findByStoreIdAndGiftCardSerialNo(Long storeId, String serialNo)`
  - `findByStoreIdAndDiscountCode(Long storeId, String discountCode)`

### 4. Time-Based Transaction Summaries
- Created `StoreTransactionSummary` DTO with:
  - Today's total and transaction count
  - Last 7 days total and transaction count
  - Last 30 days total and transaction count
- Implemented calculation logic for both gift card and discount code transactions
- Only counts actual spending (Debit transactions for gift cards, Redeem transactions for discount codes)

### 5. Store User Endpoints (Integrated into StoreController)
- Added store user functionality to existing `StoreController` with the following endpoints:
  - `GET /api/v1/store/user/transaction-summary` - Get time-based transaction summary
  - `GET /api/v1/store/user/info` - Get store information
  - `GET /api/v1/store/user/gift-card-transactions` - Get all gift card transactions for store
  - `GET /api/v1/store/user/discount-code-transactions` - Get all discount code transactions for store
  - `GET /api/v1/store/user/gift-card-transactions/search?serialNumber={serial}` - Search gift card transactions by serial
  - `GET /api/v1/store/user/discount-code-transactions/search?discountCode={code}` - Search discount code transactions by code

### 6. Authentication & Authorization
- Implemented authentication using Spring Security's `SecurityContextHolder`
- Added `getCurrentUser()` method to get authenticated user
- Added `getUserEntityByUsername()` method to `UserService`
- Store user validation ensures users have STORE_USER role and associated store

## Database Changes

### Migration V23: Add Store to Users
```sql
-- Add store_id column to users table for store users
ALTER TABLE users ADD COLUMN store_id BIGINT;

-- Add foreign key constraint
ALTER TABLE users ADD CONSTRAINT fk_user_store FOREIGN KEY (store_id) REFERENCES store(id);

-- Add index for store_id
CREATE INDEX idx_user_store ON users(store_id);
```

### Updated User Roles
```sql
-- Insert STORE_USER role
INSERT INTO user_role (name, description) VALUES
('STORE_USER', 'Store User with access to store-specific transactions');
```

## API Endpoints

### Authentication Required
All store user endpoints require Bearer token authentication.

### Transaction Summary
```
GET /api/v1/store/user/transaction-summary
```
Returns time-based transaction summary including today, last 7 days, and last 30 days totals.

**Response:**
```json
{
  "status": 0,
  "message": "Transaction summary retrieved successfully",
  "data": {
    "storeId": 1,
    "storeName": "Main Store",
    "todayTotal": 1500.00,
    "last7DaysTotal": 8500.00,
    "last30DaysTotal": 25000.00,
    "todayTransactionCount": 5,
    "last7DaysTransactionCount": 28,
    "last30DaysTransactionCount": 95
  }
}
```

### Store Information
```
GET /api/v1/store/user/info
```
Returns store information for the authenticated store user.

### Gift Card Transactions
```
GET /api/v1/store/user/gift-card-transactions
```
Returns all gift card transactions for the store.

### Discount Code Transactions
```
GET /api/v1/store/user/discount-code-transactions
```
Returns all discount code transactions for the store.

### Search Operations
```
GET /api/v1/store/user/gift-card-transactions/search?serialNumber=GC123456789
GET /api/v1/store/user/discount-code-transactions/search?discountCode=DISCOUNT10
```
Search transactions by specific criteria within the store.

## Security Features

### Role-Based Access Control
- Only users with STORE_USER role can access store user endpoints
- Users must have an associated store to access store-specific data
- All endpoints validate user authentication and authorization

### Data Isolation
- Store users can only see transactions for their associated store
- Search operations are automatically filtered by store
- No cross-store data access is possible

## Usage Examples

### Creating a Store User
1. Create a user with STORE_USER role
2. Associate the user with a specific store
3. User can then access store-specific endpoints

### Getting Transaction Summary
```bash
curl -H "Authorization: Bearer <token>" \
     http://localhost:8080/api/v1/store/user/transaction-summary
```

### Searching Gift Card Transactions
```bash
curl -H "Authorization: Bearer <token>" \
     "http://localhost:8080/api/v1/store/user/gift-card-transactions/search?serialNumber=GC123456789"
```

## Postman Collection

A complete Postman collection is available at:
`postman/StoreUserAPI_Param.postman_collection.json`

The collection includes:
- Authentication endpoints
- All store user operations
- Pre-configured variables for testing
- Example requests and responses

## Error Handling

### Common Error Responses
- **401 Unauthorized**: Invalid or missing authentication token
- **403 Forbidden**: User is not a store user or doesn't have associated store
- **404 Not Found**: Store not found for user

### Error Response Format
```json
{
  "status": -1,
  "message": "Access denied. User must be a store user with an associated store.",
  "data": null
}
```

## Future Enhancements

### Potential Improvements
1. **Pagination**: Add pagination support for large transaction lists
2. **Date Range Filtering**: Allow custom date range filtering for summaries
3. **Export Functionality**: Add CSV/Excel export for transaction data
4. **Real-time Updates**: Implement WebSocket for real-time transaction updates
5. **Advanced Analytics**: Add more detailed analytics and reporting features

### Additional Store User Capabilities
1. **Transaction Management**: Allow store users to create/update transactions
2. **Inventory Management**: Store-specific inventory tracking
3. **Customer Management**: Store-specific customer data access
4. **Reporting**: Advanced store-specific reporting capabilities

## Testing

### Unit Tests
- Test store user validation
- Test transaction summary calculations
- Test store-specific filtering
- Test authentication and authorization

### Integration Tests
- Test complete API workflows
- Test database constraints and relationships
- Test security boundaries
- Test error handling scenarios

## Monitoring and Logging

### Key Metrics to Monitor
- Store user login frequency
- Transaction summary request patterns
- Search query performance
- Error rates by endpoint

### Logging Points
- Store user authentication attempts
- Transaction summary calculations
- Search operations
- Authorization failures
- Data access patterns

This implementation provides a secure, scalable foundation for store-specific transaction management while maintaining data isolation and proper access controls.
