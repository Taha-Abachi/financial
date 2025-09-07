package com.pars.financial.exception;

public class GiftCardNotFoundException extends GenericException {
    public GiftCardNotFoundException(String message) {
        super(message);
        this.statusCode = 1;
    }

    public GiftCardNotFoundException(String message, Object data) {
        super(message, data);
        this.statusCode = 1;
    }
}
