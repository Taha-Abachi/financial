package com.pars.financial.exception;

public class ClientTransactionIdNotUniqueException extends GenericException {
    public ClientTransactionIdNotUniqueException(String message) {
        super(message);
        this.statusCode = -101;
    }

    public ClientTransactionIdNotUniqueException(String message, Object data) {
        super(message, data);
        this.statusCode = -101;
    }
}
