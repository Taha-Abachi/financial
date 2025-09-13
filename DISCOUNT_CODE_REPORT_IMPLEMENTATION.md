# Discount Code Report Implementation

## Overview
This document describes the implementation of comprehensive discount code reporting functionality that provides detailed statistics including total count, usage statistics, transaction data, and performance metrics.

## New Components

### 1. DiscountCodeReportDto
**File**: `src/main/java/com/pars/financial/dto/DiscountCodeReportDto.java`

A comprehensive DTO that encapsulates all discount code statistics:

```java
public class DiscountCodeReportDto {
    private Long totalCount;              // Total number of discount codes
    private Long totalUsedCount;          // Count of used discount codes
    private Long totalUnusedCount;        // Count of unused discount codes
    private Long totalActiveCount;        // Count of active discount codes
    private Long totalInactiveCount;      // Count of inactive discount codes
    private Long totalExpiredCount;       // Count of expired discount codes
    private Long totalRedeemTransactions; // Count of redeem transactions
    private Long totalDiscountAmount;     // Sum of all discount amounts
    private Long totalOriginalAmount;     // Sum of all original amounts
    private Double averagePercentage;     // Average discount percentage
    private Double averageMaxDiscountAmount; // Average max discount amount
    private Double averageMinimumBillAmount; // Average minimum bill amount
    private Double averageUsageCount;     // Average usage count
    private Double averageUsageLimit;     // Average usage limit
    private LocalDateTime reportGeneratedAt; // Report generation timestamp
}
```

### 2. Enhanced Repository Methods

#### DiscountCodeRepository
**File**: `src/main/java/com/pars/financial/repository/DiscountCodeRepository.java`

Added comprehensive statistics queries:

**Global Statistics:**
- `countAllDiscountCodes()` - Total discount code count
- `countUsedDiscountCodes()` - Count of used discount codes
- `countUnusedDiscountCodes()` - Count of unused discount codes
- `countActiveDiscountCodes()` - Count of active discount codes
- `countInactiveDiscountCodes()` - Count of inactive discount codes
- `countExpiredDiscountCodes(LocalDate)` - Count of expired discount codes
- `getAveragePercentage()` - Average discount percentage
- `getAverageMaxDiscountAmount()` - Average max discount amount
- `getAverageMinimumBillAmount()` - Average minimum bill amount
- `getAverageUsageCount()` - Average usage count
- `getAverageUsageLimit()` - Average usage limit

**Company-Specific Statistics:**
- `countDiscountCodesByCompany(Long)` - Company discount code count
- `countUsedDiscountCodesByCompany(Long)` - Company used count
- `countUnusedDiscountCodesByCompany(Long)` - Company unused count
- `countActiveDiscountCodesByCompany(Long)` - Company active count
- `countInactiveDiscountCodesByCompany(Long)` - Company inactive count
- `countExpiredDiscountCodesByCompany(Long, LocalDate)` - Company expired count
- `getAveragePercentageByCompany(Long)` - Company average percentage
- `getAverageMaxDiscountAmountByCompany(Long)` - Company average max discount
- `getAverageMinimumBillAmountByCompany(Long)` - Company average minimum bill
- `getAverageUsageCountByCompany(Long)` - Company average usage count
- `getAverageUsageLimitByCompany(Long)` - Company average usage limit

#### DiscountCodeTransactionRepository
**File**: `src/main/java/com/pars/financial/repository/DiscountCodeTransactionRepository.java`

Added redeem transaction statistics:

**Global Redeem Statistics:**
- `countRedeemTransactions()` - Total redeem transaction count
- `sumDiscountAmount()` - Total discount amount
- `sumOriginalAmount()` - Total original amount

**Company-Specific Redeem Statistics:**
- `countRedeemTransactionsByCompany(Long)` - Company redeem count
- `sumDiscountAmountByCompany(Long)` - Company discount amount
- `sumOriginalAmountByCompany(Long)` - Company original amount

### 3. Service Layer Enhancement

#### DiscountCodeService
**File**: `src/main/java/com/pars/financial/service/DiscountCodeService.java`

Added two new service methods:

**`generateDiscountCodeReport()`**
- Generates comprehensive report for all discount codes
- Calculates all statistics using repository methods
- Returns `DiscountCodeReportDto` with complete data
- Includes error handling and logging

**`generateDiscountCodeReportByCompany(Long companyId)`**
- Generates company-specific discount code report
- Validates company existence
- Calculates company-specific statistics
- Returns `DiscountCodeReportDto` with company data
- Includes comprehensive error handling

### 4. Controller Endpoints

#### DiscountCodeController
**File**: `src/main/java/com/pars/financial/controller/DiscountCodeController.java`

Added two new REST endpoints:

**`GET /api/v1/discountcode/report`**
- **Purpose**: Get comprehensive discount code report for all discount codes
- **Authentication**: Required (JWT or API Key)
- **Authorization**: ADMIN, API_USER, or SUPERADMIN
- **Response**: `ResponseEntity<GenericResponse<DiscountCodeReportDto>>`
- **Features**:
  - User authentication validation
  - Comprehensive error handling
  - Proper HTTP status codes
  - Detailed logging

**`GET /api/v1/discountcode/report/company/{companyId}`**
- **Purpose**: Get discount code report for specific company
- **Authentication**: Required (JWT or API Key)
- **Authorization**: ADMIN, API_USER, or SUPERADMIN
- **Parameters**: `companyId` (Long) - Company identifier
- **Response**: `ResponseEntity<GenericResponse<DiscountCodeReportDto>>`
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
.requestMatchers("/api/v1/discountcode/report").hasAnyRole("ADMIN", "API_USER", "SUPERADMIN")
.requestMatchers("/api/v1/discountcode/report/company/*").hasAnyRole("ADMIN", "API_USER", "SUPERADMIN")
```

## API Usage Examples

### 1. Get All Discount Codes Report

**Request:**
```bash
GET /api/v1/discountcode/report
Authorization: Bearer your-jwt-token
x-api-key: your-api-key
```

**Response:**
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

### 2. Get Company-Specific Report

**Request:**
```bash
GET /api/v1/discountcode/report/company/1
Authorization: Bearer your-jwt-token
x-api-key: your-api-key
```

**Response:**
```json
{
  "status": 200,
  "message": "Discount code report for company 1 generated successfully",
  "data": {
    "totalCount": 800,
    "totalUsedCount": 400,
    "totalUnusedCount": 400,
    "totalActiveCount": 700,
    "totalInactiveCount": 100,
    "totalExpiredCount": 50,
    "totalRedeemTransactions": 500,
    "totalDiscountAmount": 2000000,
    "totalOriginalAmount": 10000000,
    "averagePercentage": 18.0,
    "averageMaxDiscountAmount": 7500.0,
    "averageMinimumBillAmount": 1500.0,
    "averageUsageCount": 1.25,
    "averageUsageLimit": 1.0,
    "reportGeneratedAt": "2024-01-15T10:30:00"
  }
}
```

## Key Features

### 1. Comprehensive Statistics
- **Total Count**: Number of discount codes in the system
- **Usage Statistics**: Used vs unused discount codes
- **Status Metrics**: Active, inactive, and expired counts
- **Transaction Data**: Redeem transactions and amounts
- **Performance Metrics**: Average values across all codes

### 2. Usage Analysis
- **Used Count**: Discount codes that have been redeemed
- **Unused Count**: Discount codes that haven't been used
- **Usage Rate**: Percentage of codes that have been used
- **Average Usage**: Average number of times codes are used

### 3. Financial Metrics
- **Total Discount Amount**: Sum of all discount amounts given
- **Total Original Amount**: Sum of all original bill amounts
- **Average Discount**: Average discount percentage
- **Average Max Discount**: Average maximum discount amount
- **Average Minimum Bill**: Average minimum bill amount

### 4. Status-Based Analysis
- **Active Count**: Discount codes that are currently active
- **Inactive Count**: Discount codes that are deactivated
- **Expired Count**: Discount codes that have passed their expiry date
- **Usage Limit Analysis**: Average usage limits and current usage

### 5. Company-Specific Reporting
- All statistics can be filtered by company
- Company validation ensures data integrity
- Separate endpoints for company-specific reports

### 6. Error Handling
- Comprehensive exception handling
- Proper HTTP status codes
- Detailed error messages
- Logging for debugging and monitoring

### 7. Security
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

1. **Database Indexing**: Existing indexes on discount code fields are utilized
2. **Query Optimization**: Single queries for each statistic type
3. **Lazy Loading**: Proper entity relationships
4. **Transaction Management**: Read-only transactions for reports
5. **Caching**: Potential for future caching implementation

## Comparison with Gift Card Reports

| Feature | Gift Card Report | Discount Code Report |
|---------|------------------|---------------------|
| **Total Count** | ✅ | ✅ |
| **Financial Totals** | Balance, Initial Amount | Discount Amount, Original Amount |
| **Transaction Count** | Debit Transactions | Redeem Transactions |
| **Status Metrics** | Active, Blocked, Expired, Used | Active, Inactive, Expired, Used/Unused |
| **Average Values** | Balance, Initial Amount | Percentage, Max Discount, Min Bill, Usage |
| **Company Filtering** | ✅ | ✅ |
| **Authentication** | ✅ | ✅ |

## Future Enhancements

1. **Date Range Filtering**: Reports for specific time periods
2. **Caching**: Redis caching for frequently accessed reports
3. **Export Functionality**: CSV/Excel export capabilities
4. **Real-time Updates**: WebSocket-based real-time statistics
5. **Advanced Analytics**: Trend analysis and forecasting
6. **Dashboard Integration**: Real-time dashboard updates
7. **Discount Type Analysis**: Separate statistics by discount type (percentage vs constant)

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

This implementation provides a robust, scalable, and secure solution for discount code reporting that complements the existing gift card reporting functionality and provides comprehensive analytics for discount code usage and performance.
