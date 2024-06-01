package com.tabaldi.api.exception;

public class TabaldiGenericException extends Exception{

    private static final long serialVersionUID = 1L;
    private int code;
    public TabaldiGenericException(int code, String message) {
        super(message);
        this.code=code;
    }


    public TabaldiGenericException(String message, Throwable cause) {
        super(message, cause);
    }


    public TabaldiGenericException(Throwable cause) {
        super(cause);
        this.code=code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
