package com.pars.financial.exception;

import com.pars.financial.dto.GenericResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(ClientTransactionIdNotUniqueException.class)
    public ResponseEntity<GenericResponse> handleTransactionIdNotUniqueException(ClientTransactionIdNotUniqueException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body( new GenericResponse(ex));
    }

    @ExceptionHandler(GiftCardNotFoundException.class)
    public ResponseEntity<GenericResponse> handleTransactionIdNotUniqueException(GiftCardNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body( new GenericResponse(ex));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<GenericResponse> handleTransactionIdNotUniqueException(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericResponse(ex));
    }
}