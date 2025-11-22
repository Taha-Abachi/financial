package com.pars.financial.exception;

import com.pars.financial.constants.ErrorCodes;
import com.pars.financial.dto.GenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        logger.error("Validation error: {}", ex.getMessage());
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        ValidationException validationException = new ValidationException(ErrorCodes.INVALID_REQUEST, errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericResponse<>(validationException));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GenericResponse<?>> handleRuntimeException(RuntimeException ex) {
        logger.error("Runtime exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GenericResponse<>(ErrorCodes.SYSTEM_ERROR));
    }

    @ExceptionHandler(ClientTransactionIdNotUniqueException.class)
    public ResponseEntity<GenericResponse<?>> handleTransactionIdNotUniqueException(ClientTransactionIdNotUniqueException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GenericResponse<>(ex));
    }

    @ExceptionHandler(GiftCardNotFoundException.class)
    public ResponseEntity<GenericResponse<?>> handleGiftCardNotFoundException(GiftCardNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GenericResponse<>(ex));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<GenericResponse<?>> handleValidationException(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericResponse<>(ex));
    }

    /**
     * Maps ErrorCode to appropriate HTTP status for exceptions
     * Note: Success codes (>= 100) should not be handled by exception handlers
     * This method is available for future use if needed for dynamic error code mapping
     */
    @SuppressWarnings("unused")
    private HttpStatus getHttpStatusFromErrorCode(com.pars.financial.constants.ErrorCode errorCode) {
        int code = errorCode.getCode();
        
        // Success codes should not be handled by exception handlers
        if (code >= 100) {
            return HttpStatus.INTERNAL_SERVER_ERROR; // This should never happen in exception handlers
        } else if (code >= -10) {
            return HttpStatus.BAD_REQUEST; // General client errors
        } else if (code >= -100) {
            return HttpStatus.BAD_REQUEST; // Validation errors
        } else if (code >= -200) {
            return HttpStatus.NOT_FOUND; // Customer errors
        } else if (code >= -300) {
            return HttpStatus.NOT_FOUND; // Store errors
        } else if (code >= -400) {
            return HttpStatus.NOT_FOUND; // Company errors
        } else if (code >= -500) {
            return HttpStatus.NOT_FOUND; // Discount code errors
        } else if (code >= -600) {
            return HttpStatus.NOT_FOUND; // Gift card errors
        } else if (code >= -700) {
            return HttpStatus.BAD_REQUEST; // Transaction errors
        } else if (code >= -800) {
            return HttpStatus.UNAUTHORIZED; // Authentication errors
        } else if (code >= -900) {
            return HttpStatus.NOT_FOUND; // Batch errors
        } else if (code >= -1000) {
            return HttpStatus.NOT_FOUND; // Item category errors
        } else if (code >= -1100) {
            return HttpStatus.NOT_FOUND; // User role errors
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR; // System errors
        }
    }
}