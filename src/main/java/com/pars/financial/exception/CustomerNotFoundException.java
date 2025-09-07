package com.pars.financial.exception;

public class CustomerNotFoundException extends GenericException {

    public CustomerNotFoundException(String message) {
        super(message);
        this.statusCode = -102;
    }

    public CustomerNotFoundException(String message, Object data) {
        super(message, data);
        this.statusCode = -102;
    }
}
