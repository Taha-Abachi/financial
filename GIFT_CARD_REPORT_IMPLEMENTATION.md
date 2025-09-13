# Gift Card Report Implementation

## Overview
This document describes the implementation of comprehensive gift card reporting functionality that provides detailed statistics including total count, total balance, total initial amount, and total debit transactions.

## New Components

### 1. GiftCardReportDto
**File**: `src/main/java/com/pars/financial/dto/GiftCardReportDto.java`

A comprehensive DTO that encapsulates all gift card statistics:

```java
public class GiftCardReportDto {
    private Long totalCount;              // Total number of gift cards
    private Long totalBalance;            // Sum of all current balances
    private Long totalInitialAmount;      // Sum of all initial amounts
    private Long totalDebitTransactions;  // Count of debit transactions
    private Long totalDebitAmount;        // Sum of all debit amounts
    private Long activeCount;             // Count of active gift cards
    private Long blockedCount;            // Count of blocked gift cards
    private Long expiredCount;            // Count of expired gift cards
    private Long usedCount;               // Count of used gift cards
    private Double averageBalance;        // Average balance across all gift cards
    private Double averageInitialAmount;  // Average initial amount
    private LocalDateTime reportGeneratedAt; // Report generation timestamp
}
```

### 2. Enhanced Repository Methods

#### GiftCardRepository
**File**: `src/main/java/com/pars/financial/repository/GiftCardRepository.java`

Added comprehensive statistics queries:

**Global Statistics:**
- `countAllGiftCards()` - Total gift card count
- `sumTotalBalance()` - Sum of all balances
- `sumTotalInitialAmount()` - Sum of all initial amounts
- `countActiveGiftCards()` - Count of active gift cards
- `countBlockedGiftCards()` - Count of blocked gift cards
- `countExpiredGiftCards(LocalDate)` - Count of expired gift cards
- `countUsedGiftCards()` - Count of used gift cards
- `getAverageBalance()` - Average balance
- `getAverageInitialAmount()` - Average initial amount

**Company-Specific Statistics:**
- `countGiftCardsByCompany(Long)` - Company gift card count
- `sumBalanceByCompany(Long)` - Company balance sum
- `sumInitialAmountByCompany(Long)` - Company initial amount sum
- `countActiveGiftCardsByCompany(Long)` - Company active count
- `countBlockedGiftCardsByCompany(Long)` - Company blocked count
- `countExpiredGiftCardsByCompany(Long, LocalDate)` - Company expired count
- `countUsedGiftCardsByCompany(Long)` - Company used count
- `getAverageBalanceByCompany(Long)` - Company average balance
- `getAverageInitialAmountByCompany(Long)` - Company average initial amount

#### GiftCardTransactionRepository
**File**: `src/main/java/com/pars/financial/repository/GiftCardTransactionRepository.java`

Added debit transaction statistics:

**Global Debit Statistics:**
- `countDebitTransactions()` - Total debit transaction count
- `sumDebitAmount()` - Total debit amount

**Company-Specific Debit Statistics:**
- `countDebitTransactionsByCompany(Long)` - Company debit count
- `sumDebitAmountByCompany(Long)` - Company debit amount

### 3. Service Layer Enhancement

#### GiftCardService
**File**: `src/main/java/com/pars/financial/service/GiftCardService.java`

Added two new service methods:

**`generateGiftCardReport()`**
- Generates comprehensive report for all gift cards
- Calculates all statistics using repository methods
- Returns `GiftCardReportDto` with complete data
- Includes error handling and logging

**`generateGiftCardReportByCompany(Long companyId)`**
- Generates company-specific gift card report
- Validates company existence
- Calculates company-specific statistics
- Returns `GiftCardReportDto` with company data
- Includes comprehensive error handling

### 4. Controller Endpoints

#### GiftCardController
**File**: `src/main/java/com/pars/financial/controller/GiftCardController.java`

Added two new REST endpoints:

**`GET /api/v1/giftcard/report`**
- **Purpose**: Get comprehensive gift card report for all gift cards
- **Authentication**: Required (JWT or API Key)
- **Authorization**: ADMIN, API_USER, or SUPERADMIN
- **Response**: `ResponseEntity<GenericResponse<GiftCardReportDto>>`
- **Features**:
  - User authentication validation
  - Comprehensive error handling
  - Proper HTTP status codes
  - Detailed logging

**`GET /api/v1/giftcard/report/company/{companyId}`**
- **Purpose**: Get gift card report for specific company
- **Authentication**: Required (JWT or API Key)
- **Authorization**: ADMIN, API_USER, or SUPERADMIN
- **Parameters**: `companyId` (Long) - Company identifier
- **Response**: `ResponseEntity<GenericResponse<GiftCardReportDto>>`
- **Features**:
  - User authentication validation
  - Company validation
  - Comprehensive error handling
  - Proper HTTP status codes
  - Detailed logging

### 5. Security Configuration

#### SecurityConfig
**File**: `src/main/java/com/pars/financial/configuration/SecurityConfig.java`

Added security rules for new endpoints:

```java
.requestMatchers("/api/v1/giftcard/report").hasAnyRole("ADMIN", "API_USER", "SUPERADMIN")
.requestMatchers("/api/v1/giftcard/report/company/*").hasAnyRole("ADMIN", "API_USER", "SUPERADMIN")
```

## API Usage Examples

### 1. Get All Gift Cards Report

**Request:**
```bash
GET /api/v1/giftcard/report
Authorization: Bearer your-jwt-token
x-api-key: your-api-key
```

**Response:**
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

### 2. Get Company-Specific Report

**Request:**
```bash
GET /api/v1/giftcard/report/company/1
Authorization: Bearer your-jwt-token
x-api-key: your-api-key
```

**Response:**
```json
{
  "status": 200,
  "message": "Gift card report for company 1 generated successfully",
  "data": {
    "totalCount": 500,
    "totalBalance": 2500000,
    "totalInitialAmount": 3000000,
    "totalDebitTransactions": 800,
    "totalDebitAmount": 500000,
    "activeCount": 400,
    "blockedCount": 20,
    "expiredCount": 50,
    "usedCount": 300,
    "averageBalance": 6250.0,
    "averageInitialAmount": 6000.0,
    "reportGeneratedAt": "2024-01-15T10:30:00"
  }
}
```

## Key Features

### 1. Comprehensive Statistics
- **Total Count**: Number of gift cards in the system
- **Total Balance**: Sum of all current balances
- **Total Initial Amount**: Sum of all initial amounts issued
- **Total Debit Transactions**: Count of all debit transactions
- **Total Debit Amount**: Sum of all debit transaction amounts

### 2. Status-Based Metrics
- **Active Count**: Gift cards that are active and not blocked
- **Blocked Count**: Gift cards that are blocked
- **Expired Count**: Gift cards that have passed their expiry date
- **Used Count**: Gift cards that have been partially or fully used

### 3. Average Calculations
- **Average Balance**: Mean balance across all gift cards
- **Average Initial Amount**: Mean initial amount across all gift cards

### 4. Company-Specific Reporting
- All statistics can be filtered by company
- Company validation ensures data integrity
- Separate endpoints for company-specific reports

### 5. Error Handling
- Comprehensive exception handling
- Proper HTTP status codes
- Detailed error messages
- Logging for debugging and monitoring

### 6. Security
- Authentication required for all endpoints
- Role-based access control
- Support for both JWT and API Key authentication

## Database Queries

The implementation uses optimized JPA queries with:

- **COALESCE**: Handles null values gracefully
- **Aggregate Functions**: COUNT, SUM, AVG for statistics
- **Conditional Logic**: WHERE clauses for filtering
- **Date Comparisons**: For expiry date calculations
- **Join Operations**: For company-specific statistics

## Performance Considerations

1. **Database Indexing**: Existing indexes on gift card fields are utilized
2. **Query Optimization**: Single queries for each statistic type
3. **Lazy Loading**: Proper entity relationships
4. **Transaction Management**: Read-only transactions for reports
5. **Caching**: Potential for future caching implementation

## Future Enhancements

1. **Date Range Filtering**: Reports for specific time periods
2. **Caching**: Redis caching for frequently accessed reports
3. **Export Functionality**: CSV/Excel export capabilities
4. **Real-time Updates**: WebSocket-based real-time statistics
5. **Advanced Analytics**: Trend analysis and forecasting
6. **Dashboard Integration**: Real-time dashboard updates

## Testing

The implementation includes:
- **Unit Tests**: For service layer methods
- **Integration Tests**: For repository queries
- **Controller Tests**: For endpoint functionality
- **Security Tests**: For authentication and authorization

## Monitoring and Logging

- **Comprehensive Logging**: All operations are logged
- **Performance Metrics**: Query execution times
- **Error Tracking**: Detailed error logging
- **Audit Trail**: Report generation tracking

This implementation provides a robust, scalable, and secure solution for gift card reporting that meets the requirements for comprehensive statistics and analytics.
