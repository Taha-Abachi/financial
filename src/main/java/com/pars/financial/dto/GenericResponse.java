package com.pars.financial.dto;

import com.pars.financial.exception.GenericException;

public class GenericResponse<T> {
    public int status = 0;
    public String message = "Success";
    public String messageFa = "موفقیت";
    public T data = null;

    public GenericResponse() {}

    public GenericResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public GenericResponse(int status, String message, String messageFa, T data) {
        this.status = status;
        this.message = message;
        this.messageFa = messageFa;
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public GenericResponse(GenericException genericException) {
        this.status = genericException.statusCode;
        this.message = genericException.getMessage();
        this.messageFa = genericException.messageFa != null ? genericException.messageFa : "خطا";
        this.data = (T) genericException.data;
    }
}

