package com.pars.financial.exception;

public class GenericException extends RuntimeException {
    public int statusCode;
    public Object data;
    public GenericException(String message) {
        super(message);
    }
    public GenericException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public GenericException(String message, Object data, int statusCode) {
        super(message);
        this.data = data;
        this.statusCode = statusCode;
    }
}
