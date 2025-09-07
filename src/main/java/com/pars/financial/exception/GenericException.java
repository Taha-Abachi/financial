package com.pars.financial.exception;

public class GenericException extends RuntimeException {
    public int statusCode;
    public Object data;
    public String messageFa;

    public GenericException(String message) {
        super(message);
    }

    public GenericException(String message, String messageFa) {
        super(message);
        this.messageFa = messageFa;
    }

    public GenericException(String message, Object data) {
        super(message);
        this.data = data;
    }

    public GenericException(String message, String messageFa, Object data) {
        super(message);
        this.messageFa = messageFa;
        this.data = data;
    }

    public GenericException(String message, Object data, int statusCode) {
        super(message);
        this.data = data;
        this.statusCode = statusCode;
    }

    public GenericException(String message, String messageFa, Object data, int statusCode) {
        super(message);
        this.messageFa = messageFa;
        this.data = data;
        this.statusCode = statusCode;
    }
}
