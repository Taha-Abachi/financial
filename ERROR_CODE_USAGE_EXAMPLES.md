# Error Code System Usage Examples

## Overview
This document shows how to use the new ErrorCode system for consistent error handling with Persian and English messages.

## Basic Usage

### 1. Throwing Exceptions with Error Codes

```java
// Instead of this (old way):
throw new ValidationException("Phone number is required", null, -103);

// Use this (new way):
throw new ErrorCodeException(ErrorCodes.CUSTOMER_PHONE_REQUIRED);
```

### 2. Controller Examples

#### Before (Old Way):
```java
@GetMapping("/{id}")
public GenericResponse<Customer> getCustomer(@PathVariable Long id) {
    var response = new GenericResponse<Customer>();
    try {
        var customer = customerService.findById(id);
        if (customer == null) {
            response.status = -1;
            response.message = "Customer not found";
            response.messageFa = "مشتری یافت نشد";
            return response;
        }
        response.data = customer;
    } catch (Exception e) {
        response.status = -1;
        response.message = e.getMessage();
        response.messageFa = "خطا";
    }
    return response;
}
```

#### After (New Way):
```java
@GetMapping("/{id}")
public GenericResponse<Customer> getCustomer(@PathVariable Long id) {
    try {
        var customer = customerService.findById(id);
        if (customer == null) {
            throw new ErrorCodeException(ErrorCodes.CUSTOMER_NOT_FOUND);
        }
        return new GenericResponse<>(ErrorCodes.SUCCESS, customer);
    } catch (ErrorCodeException e) {
        throw e; // Let GlobalExceptionHandler handle it
    } catch (Exception e) {
        throw new ErrorCodeException(ErrorCodes.SYSTEM_ERROR);
    }
}
```

### 3. Service Layer Examples

#### Before (Old Way):
```java
public DiscountCodeDto getDiscountCode(String code) {
    if (code == null || code.trim().isEmpty()) {
        throw new ValidationException("Discount code is required", null, -100);
    }
    
    var discountCode = discountCodeRepository.findByCode(code);
    if (discountCode == null) {
        throw new ValidationException("Discount code not found", null, -104);
    }
    
    if (!discountCode.isActive()) {
        throw new ValidationException("Discount code is inactive", null, -105);
    }
    
    return convertToDto(discountCode);
}
```

#### After (New Way):
```java
public DiscountCodeDto getDiscountCode(String code) {
    if (code == null || code.trim().isEmpty()) {
        throw new ErrorCodeException(ErrorCodes.REQUIRED_FIELD_MISSING);
    }
    
    var discountCode = discountCodeRepository.findByCode(code);
    if (discountCode == null) {
        throw new ErrorCodeException(ErrorCodes.DISCOUNT_CODE_NOT_FOUND);
    }
    
    if (!discountCode.isActive()) {
        throw new ErrorCodeException(ErrorCodes.DISCOUNT_CODE_INACTIVE);
    }
    
    return convertToDto(discountCode);
}
```

### 4. Batch Report Controller Example

#### Before (Old Way):
```java
@GetMapping("/{batchId}/report")
public ResponseEntity<GenericResponse<BatchReportDto>> getBatchReport(@PathVariable Long batchId) {
    var response = new GenericResponse<BatchReportDto>();
    
    try {
        BatchReportDto report = batchReportService.generateBatchReport(batchId);
        if (report == null) {
            response.message = "Batch not found";
            response.messageFa = "دسته‌بندی یافت نشد";
            response.status = 404;
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        response.data = report;
        response.message = "Batch report generated successfully";
        response.messageFa = "گزارش دسته‌بندی با موفقیت تولید شد";
        response.status = 200;
        return ResponseEntity.ok(response);
        
    } catch (Exception e) {
        response.message = "Error generating batch report: " + e.getMessage();
        response.messageFa = "خطا در تولید گزارش دسته‌بندی: " + e.getMessage();
        response.status = 500;
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

#### After (New Way):
```java
@GetMapping("/{batchId}/report")
public ResponseEntity<GenericResponse<BatchReportDto>> getBatchReport(@PathVariable Long batchId) {
    try {
        BatchReportDto report = batchReportService.generateBatchReport(batchId);
        if (report == null) {
            throw new ErrorCodeException(ErrorCodes.BATCH_NOT_FOUND);
        }
        
        return ResponseEntity.ok(new GenericResponse<>(ErrorCodes.REPORT_GENERATED_SUCCESSFULLY, report));
        
    } catch (ErrorCodeException e) {
        throw e; // Let GlobalExceptionHandler handle it
    } catch (Exception e) {
        throw new ErrorCodeException(ErrorCodes.BATCH_REPORT_GENERATION_FAILED);
    }
}
```

## Response Examples

### Success Response:
```json
{
  "status": 0,
  "message": "Success",
  "messageFa": "موفقیت",
  "data": { ... }
}
```

### Error Response:
```json
{
  "status": -500,
  "message": "Discount code not found",
  "messageFa": "کد تخفیف یافت نشد",
  "data": null
}
```

### Success with Data:
```json
{
  "status": 106,
  "message": "Report generated successfully",
  "messageFa": "گزارش با موفقیت تولید شد",
  "data": { ... }
}
```

## Benefits

1. **Consistency**: All error messages are centralized and consistent
2. **Maintainability**: Easy to update messages in one place
3. **Internationalization**: Automatic Persian and English support
4. **Type Safety**: Error codes are constants, preventing typos
5. **Clean Code**: Controllers are much cleaner and focused on business logic
6. **Automatic HTTP Status**: GlobalExceptionHandler automatically maps error codes to HTTP status codes

## Migration Strategy

1. **Phase 1**: Start using ErrorCodeException in new code
2. **Phase 2**: Gradually migrate existing controllers
3. **Phase 3**: Remove old exception handling patterns
4. **Phase 4**: Add more specific error codes as needed

## Adding New Error Codes

To add a new error code:

1. Add it to `ErrorCodes.java`:
```java
public static final ErrorCode NEW_ERROR = new ErrorCode(-1200, "New error message", "پیام خطای جدید");
```

2. Add it to the switch statement in `getByCode()` method

3. Use it in your code:
```java
throw new ErrorCodeException(ErrorCodes.NEW_ERROR);
```
