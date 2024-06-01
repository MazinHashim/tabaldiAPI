package com.tabaldi.api.exception;

import jakarta.servlet.http.HttpServletResponse;

public class CustomErrorResponse {
    private String message;
    private int code;

    public CustomErrorResponse() {}

    public CustomErrorResponse(HttpServletResponse response) {
        this.code = response.getStatus();
    }

    public CustomErrorResponse(TabaldiGenericException exception) {
        this.code = exception.getCode();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
