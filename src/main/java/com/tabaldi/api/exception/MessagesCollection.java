package com.tabaldi.api.exception;

public enum MessagesCollection {
    SUCCESS_FETCHING("{{DATA}} Fetched Successfully"),
    SUCCESS_ADDITION("{{DATA}} Added Successfully"),
    SUCCESS_OTP_SEND("OTP Code Sent Successfully"),
    SUCCESS_LOGIN("User login successfully"),
    SUCCESS_PHONE_VERIFICATION("Phone number verified successfully"),
    SUCCESS_LOGOUT("User Session Logged out Successfully"),
    SUCCESS_SESSION_REFRESH("User Session Has Been Refreshed");
    MessagesCollection(String message) {
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
