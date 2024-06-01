package com.tabaldi.api.exception;

public enum ErrorMessagesCollection {
    UNEXPECTED_ERROR("Unexpected Error"),
    BAD_REQUEST("Bad Request"),
    BAD_CREDENTIALS("Sorry, wrong email or password"),
    OBJECT_NOT_FOUND("{{OBJECT}} not found"),
    FORMAT_NOT_VALID("{{DATA}} not valid {{TYPE}} format"),
    CODE_NOT_EXPIRED("Last otp code not expired"),
    EXCEED_RESEND_LIMIT("Cannot resend for now try again later"),
    VERIFICATION_CODE_EXPIRED("Otp verification code is expired"),
    ;
    ErrorMessagesCollection(String message) {
        this.message = message;
    }
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
