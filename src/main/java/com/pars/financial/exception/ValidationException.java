package com.pars.financial.exception;

import com.pars.financial.constants.ErrorCode;
import com.pars.financial.constants.ErrorCodes;

/**
 * Enhanced ValidationException that works with ErrorCode system
 * Maintains backward compatibility while providing new ErrorCode functionality
 */
public class ValidationException extends GenericException {
    
    // ===== NEW ERRORCODE-BASED CONSTRUCTORS =====
    
    /**
     * Create validation exception with ErrorCode
     */
    public ValidationException(ErrorCode errorCode) {
        super(errorCode.getMessageEn(), errorCode.getMessageFa());
        this.statusCode = errorCode.getCode();
    }
    
    /**
     * Create validation exception with ErrorCode and additional data
     */
    public ValidationException(ErrorCode errorCode, Object data) {
        super(errorCode.getMessageEn(), errorCode.getMessageFa(), data);
        this.statusCode = errorCode.getCode();
    }
    
    /**
     * Create validation exception with ErrorCode and custom message
     */
    public ValidationException(ErrorCode errorCode, String customMessage) {
        super(customMessage, errorCode.getMessageFa());
        this.statusCode = errorCode.getCode();
    }
    
    /**
     * Create validation exception with ErrorCode, custom message, and data
     */
    public ValidationException(ErrorCode errorCode, String customMessage, Object data) {
        super(customMessage, errorCode.getMessageFa(), data);
        this.statusCode = errorCode.getCode();
    }
    
    // ===== BACKWARD COMPATIBILITY CONSTRUCTORS =====
    
    /**
     * @deprecated Use ValidationException(ErrorCode) instead
     */
    @Deprecated
    public ValidationException(String message) {
        super(message);
        this.statusCode = -100;
    }

    /**
     * @deprecated Use ValidationException(ErrorCode) instead
     */
    @Deprecated
    public ValidationException(String message, String messageFa) {
        super(message, messageFa);
        this.statusCode = -100;
    }

    /**
     * @deprecated Use ValidationException(ErrorCode, Object) instead
     */
    @Deprecated
    public ValidationException(String message, Object data) {
        super(message, data);
        this.statusCode = -100;
    }

    /**
     * @deprecated Use ValidationException(ErrorCode, Object) instead
     */
    @Deprecated
    public ValidationException(String message, String messageFa, Object data) {
        super(message, messageFa, data);
        this.statusCode = -100;
    }

    /**
     * @deprecated Use ValidationException(ErrorCode) instead
     */
    @Deprecated
    public ValidationException(String message, Object data, int status) {
        super(message, data, status);
        this.statusCode = status;
    }

    /**
     * @deprecated Use ValidationException(ErrorCode, Object) instead
     */
    @Deprecated
    public ValidationException(String message, String messageFa, Object data, int status) {
        super(message, messageFa, data, status);
        this.statusCode = status;
    }
    
    // ===== CONVENIENCE FACTORY METHODS =====
    
    /**
     * Create validation exception for required field missing
     */
    public static ValidationException requiredFieldMissing(String fieldName) {
        return new ValidationException(ErrorCodes.REQUIRED_FIELD_MISSING, 
            "Required field missing: " + fieldName);
    }
    
    /**
     * Create validation exception for invalid format
     */
    public static ValidationException invalidFormat(String fieldName, String expectedFormat) {
        return new ValidationException(ErrorCodes.INVALID_FORMAT, 
            "Invalid format for " + fieldName + ". Expected: " + expectedFormat);
    }
    
    /**
     * Create validation exception for invalid amount
     */
    public static ValidationException invalidAmount(String amount) {
        return new ValidationException(ErrorCodes.INVALID_AMOUNT, 
            "Invalid amount: " + amount);
    }
    
    /**
     * Create validation exception for invalid phone number
     */
    public static ValidationException invalidPhoneNumber(String phoneNumber) {
        return new ValidationException(ErrorCodes.INVALID_PHONE_NUMBER, 
            "Invalid phone number: " + phoneNumber);
    }
    
    /**
     * Create validation exception for invalid email
     */
    public static ValidationException invalidEmail(String email) {
        return new ValidationException(ErrorCodes.INVALID_EMAIL, 
            "Invalid email: " + email);
    }
    
    /**
     * Create validation exception for invalid date
     */
    public static ValidationException invalidDate(String date) {
        return new ValidationException(ErrorCodes.INVALID_DATE, 
            "Invalid date: " + date);
    }
    
    /**
     * Create validation exception for invalid status
     */
    public static ValidationException invalidStatus(String status) {
        return new ValidationException(ErrorCodes.INVALID_STATUS, 
            "Invalid status: " + status);
    }
    
    /**
     * Create validation exception for invalid role
     */
    public static ValidationException invalidRole(String role) {
        return new ValidationException(ErrorCodes.INVALID_ROLE, 
            "Invalid role: " + role);
    }
}