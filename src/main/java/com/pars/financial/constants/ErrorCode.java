package com.pars.financial.constants;

/**
 * Represents an error code with English and Persian messages
 */
public class ErrorCode {
    private final int code;
    private final String messageEn;
    private final String messageFa;
    
    public ErrorCode(int code, String messageEn, String messageFa) {
        this.code = code;
        this.messageEn = messageEn;
        this.messageFa = messageFa;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessageEn() {
        return messageEn;
    }
    
    public String getMessageFa() {
        return messageFa;
    }
    
    @Override
    public String toString() {
        return String.format("ErrorCode{code=%d, messageEn='%s', messageFa='%s'}", 
                           code, messageEn, messageFa);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ErrorCode errorCode = (ErrorCode) obj;
        return code == errorCode.code;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(code);
    }
}
