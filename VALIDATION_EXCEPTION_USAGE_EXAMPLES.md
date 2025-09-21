# Validation Exception Usage Examples

## Overview
This document shows how to use the enhanced `ValidationException` with the new ErrorCode system for consistent validation error handling.

## 🎯 **Three Ways to Use Validation Exceptions**

### **1. Using ErrorCode Constants (Recommended)**
```java
// Simple validation with ErrorCode
throw new ValidationException(ErrorCodes.REQUIRED_FIELD_MISSING);

// With custom message
throw new ValidationException(ErrorCodes.INVALID_PHONE_NUMBER, "Phone number format is incorrect");

// With data
throw new ValidationException(ErrorCodes.INVALID_AMOUNT, "Amount must be positive", amount);
```

### **2. Using Factory Methods (Most Convenient)**
```java
// Required field missing
throw ValidationException.requiredFieldMissing("phoneNumber");

// Invalid format
throw ValidationException.invalidFormat("email", "user@example.com");

// Invalid amount
throw ValidationException.invalidAmount("-100");

// Invalid phone number
throw ValidationException.invalidPhoneNumber("123");

// Invalid email
throw ValidationException.invalidEmail("invalid-email");

// Invalid date
throw ValidationException.invalidDate("2024-13-45");

// Invalid status
throw ValidationException.invalidStatus("INVALID_STATUS");

// Invalid role
throw ValidationException.invalidRole("SUPER_USER");
```

### **3. Using ErrorCodeException (Alternative)**
```java
// Same as ValidationException but different exception type
throw new ErrorCodeException(ErrorCodes.REQUIRED_FIELD_MISSING);
throw new ErrorCodeException(ErrorCodes.INVALID_PHONE_NUMBER, "Custom message");
```

## 📝 **Controller Examples**

### **Before (Old Way):**
```java
@PostMapping("/create")
public GenericResponse<Customer> createCustomer(@RequestBody CustomerDto dto) {
    var response = new GenericResponse<Customer>();
    
    if (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty()) {
        response.status = -101;
        response.message = "Phone number is required";
        response.messageFa = "شماره تلفن الزامی است";
        return response;
    }
    
    if (!isValidPhoneNumber(dto.getPhoneNumber())) {
        response.status = -104;
        response.message = "Invalid phone number format";
        response.messageFa = "فرمت شماره تلفن نامعتبر است";
        return response;
    }
    
    if (dto.getEmail() != null && !isValidEmail(dto.getEmail())) {
        response.status = -105;
        response.message = "Invalid email format";
        response.messageFa = "فرمت ایمیل نامعتبر است";
        return response;
    }
    
    // ... business logic
    return response;
}
```

### **After (New Way):**
```java
@PostMapping("/create")
public GenericResponse<Customer> createCustomer(@RequestBody CustomerDto dto) {
    // Validation
    if (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty()) {
        throw ValidationException.requiredFieldMissing("phoneNumber");
    }
    
    if (!isValidPhoneNumber(dto.getPhoneNumber())) {
        throw ValidationException.invalidPhoneNumber(dto.getPhoneNumber());
    }
    
    if (dto.getEmail() != null && !isValidEmail(dto.getEmail())) {
        throw ValidationException.invalidEmail(dto.getEmail());
    }
    
    // Business logic
    var customer = customerService.createCustomer(dto);
    return new GenericResponse<>(ErrorCodes.CREATED_SUCCESSFULLY, customer);
}
```

## 🔧 **Service Layer Examples**

### **Validation Service:**
```java
@Service
public class ValidationService {
    
    public void validateCustomer(CustomerDto dto) {
        if (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty()) {
            throw ValidationException.requiredFieldMissing("phoneNumber");
        }
        
        if (!isValidPhoneNumber(dto.getPhoneNumber())) {
            throw ValidationException.invalidPhoneNumber(dto.getPhoneNumber());
        }
        
        if (dto.getEmail() != null && !isValidEmail(dto.getEmail())) {
            throw ValidationException.invalidEmail(dto.getEmail());
        }
        
        if (dto.getAmount() != null && dto.getAmount() <= 0) {
            throw ValidationException.invalidAmount(dto.getAmount().toString());
        }
    }
    
    public void validateDiscountCode(DiscountCodeDto dto) {
        if (dto.getCode() == null || dto.getCode().trim().isEmpty()) {
            throw ValidationException.requiredFieldMissing("code");
        }
        
        if (dto.getDiscountAmount() == null || dto.getDiscountAmount() <= 0) {
            throw ValidationException.invalidAmount("discountAmount");
        }
        
        if (dto.getExpiryDate() != null && dto.getExpiryDate().isBefore(LocalDate.now())) {
            throw ValidationException.invalidDate("expiryDate");
        }
    }
    
    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^\\+?[1-9]\\d{1,14}$");
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
```

### **Controller Using Validation Service:**
```java
@PostMapping("/create")
public GenericResponse<Customer> createCustomer(@RequestBody CustomerDto dto) {
    validationService.validateCustomer(dto);
    var customer = customerService.createCustomer(dto);
    return new GenericResponse<>(ErrorCodes.CREATED_SUCCESSFULLY, customer);
}
```

## 📊 **Response Examples**

### **Required Field Missing:**
```json
{
  "status": -101,
  "message": "Required field missing: phoneNumber",
  "messageFa": "فیلد اجباری خالی است",
  "data": null
}
```

### **Invalid Phone Number:**
```json
{
  "status": -104,
  "message": "Invalid phone number: 123",
  "messageFa": "شماره تلفن نامعتبر است",
  "data": null
}
```

### **Invalid Email:**
```json
{
  "status": -105,
  "message": "Invalid email: invalid-email",
  "messageFa": "ایمیل نامعتبر است",
  "data": null
}
```

### **Invalid Amount:**
```json
{
  "status": -103,
  "message": "Invalid amount: -100",
  "messageFa": "مبلغ نامعتبر است",
  "data": null
}
```

## 🎯 **Migration Strategy**

### **Phase 1: Start Using Factory Methods**
Replace existing validation code with factory methods:
```java
// Old
if (phone == null) {
    throw new ValidationException("Phone is required", null, -101);
}

// New
if (phone == null) {
    throw ValidationException.requiredFieldMissing("phone");
}
```

### **Phase 2: Use ErrorCode Constants**
For more specific validation errors:
```java
// Old
throw new ValidationException("Invalid data", null, -100);

// New
throw new ValidationException(ErrorCodes.VALIDATION_ERROR);
```

### **Phase 3: Create Custom Validation Methods**
For complex validation logic:
```java
public static ValidationException invalidDiscountCode(String code, String reason) {
    return new ValidationException(ErrorCodes.DISCOUNT_CODE_INVALID, 
        "Invalid discount code '" + code + "': " + reason);
}
```

## ✅ **Benefits**

1. **Consistent Error Messages**: All validation errors use the same format
2. **Automatic Translation**: Persian messages are automatically included
3. **Type Safety**: Error codes are constants, preventing typos
4. **Clean Code**: Controllers are much cleaner and focused on business logic
5. **Easy Migration**: Backward compatibility with existing code
6. **Convenient Factory Methods**: Easy-to-use static methods for common validations
7. **Centralized Error Codes**: All error codes in one place

## 🚀 **Best Practices**

1. **Use factory methods** for common validation errors
2. **Use ErrorCode constants** for specific business logic errors
3. **Create custom validation methods** for complex validation logic
4. **Validate early** in the controller or service layer
5. **Let GlobalExceptionHandler** handle the exceptions automatically
6. **Use descriptive field names** in validation messages

The enhanced ValidationException provides a clean, consistent way to handle validation errors with automatic Persian translation support! 🎉
