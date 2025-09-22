# Transaction Aggregations API Documentation

## Overview
The Transaction Aggregations API provides comprehensive analytics for gift card transactions across different time periods and transaction statuses. This endpoint is designed to support both store-level and system-wide reporting based on user roles.

## Endpoint
**GET** `/api/v1/giftcard/transaction/aggregations`

## Authentication
- **Header**: `X-API-Key: your-api-key`
- **Required**: Yes

## Authorization
- **STORE_USER**: Returns aggregations only for their assigned store
- **SUPERADMIN**: Returns aggregations for all stores
- **Other Roles**: Returns aggregations for all stores

## Response Structure

### Success Response (200 OK)
```json
{
  "status": 0,
  "message": "Success",
  "data": {
    "today": [
      {
        "status": "Confirmed",
        "count": 15,
        "totalAmount": 150000,
        "totalOrderAmount": 200000
      },
      {
        "status": "Refunded",
        "count": 2,
        "totalAmount": 20000,
        "totalOrderAmount": 25000
      },
      {
        "status": "Reversed",
        "count": 1,
        "totalAmount": 10000,
        "totalOrderAmount": 12000
      }
    ],
    "last7Days": [...],
    "last30Days": [...]
  }
}
```

### Error Response (401 Unauthorized)
```json
{
  "status": 401,
  "message": "Invalid or missing API key",
  "data": null
}
```

## Response Fields

### Time Periods
- **today**: Aggregations for transactions from start of today to end of today
- **last7Days**: Aggregations for transactions from 7 days ago to end of today
- **last30Days**: Aggregations for transactions from 30 days ago to end of today

### Transaction Status Aggregations
Each time period contains an array of status aggregations:

- **status**: Transaction status (`Confirmed`, `Refunded`, `Reversed`)
- **count**: Number of transactions with this status
- **totalAmount**: Sum of transaction amounts for this status
- **totalOrderAmount**: Sum of order amounts for this status

## Transaction Statuses

### Confirmed
- **Description**: Successfully completed transactions
- **Use Case**: Track successful gift card usage

### Refunded
- **Description**: Transactions that have been refunded
- **Use Case**: Track refunded amounts and frequency

### Reversed
- **Description**: Unsuccessful or reversed transactions
- **Use Case**: Track failed transactions and system issues

## Data Guarantees

### Always Present
The API **always** returns:
- Exactly 3 status entries for each time period
- All fields populated (count, totalAmount, totalOrderAmount)
- Zero values when no transactions exist for a status
- Consistent structure regardless of data availability

### Example - No Data Scenario
```json
{
  "status": 0,
  "message": "Success",
  "data": {
    "today": [
      {
        "status": "Confirmed",
        "count": 0,
        "totalAmount": 0,
        "totalOrderAmount": 0
      },
      {
        "status": "Refunded",
        "count": 0,
        "totalAmount": 0,
        "totalOrderAmount": 0
      },
      {
        "status": "Reversed",
        "count": 0,
        "totalAmount": 0,
        "totalOrderAmount": 0
      }
    ],
    "last7Days": [...],
    "last30Days": [...]
  }
}
```

## Usage Examples

### Store User (Limited Data)
A STORE_USER will only see data for their assigned store:
```json
{
  "data": {
    "today": [
      {
        "status": "Confirmed",
        "count": 5,
        "totalAmount": 50000,
        "totalOrderAmount": 75000
      }
      // ... other statuses
    ]
  }
}
```

### Super Admin (All Data)
A SUPERADMIN will see data for all stores:
```json
{
  "data": {
    "today": [
      {
        "status": "Confirmed",
        "count": 150,
        "totalAmount": 1500000,
        "totalOrderAmount": 2000000
      }
      // ... other statuses
    ]
  }
}
```

## Business Use Cases

### Store Performance Analysis
- Track daily, weekly, and monthly transaction volumes
- Monitor success rates (Confirmed vs Reversed)
- Analyze refund patterns and amounts

### Financial Reporting
- Calculate total transaction values by status
- Track order amounts vs transaction amounts
- Generate time-based financial reports

### System Health Monitoring
- Monitor transaction failure rates (Reversed status)
- Track refund frequency and amounts
- Identify system issues through transaction patterns

## Postman Collection
The endpoint is included in the "Gift Card API" Postman collection with multiple example responses:
- Success Response - SUPERADMIN
- Success Response - STORE_USER  
- Success Response - No Data
- Error Response - Unauthorized

## Notes
- All amounts are returned in the smallest currency unit (e.g., cents)
- Date ranges are calculated based on the server's timezone
- The API only considers 'Debit' transaction types for aggregations
- Results are cached for performance optimization
