package com.pars.financial.exception;


public class ValidationException extends GenericException {
    public ValidationException(String message) {
        super(message);
        this.statusCode = -100;
    }

    public ValidationException(String message, Object data) {
        super(message, data);
        this.statusCode = -100;
    }

    public ValidationException(String message, Object data, int status) {
        super(message, data, status);
        this.statusCode = status;
    }
}