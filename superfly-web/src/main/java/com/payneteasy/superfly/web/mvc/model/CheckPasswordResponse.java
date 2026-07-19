package com.payneteasy.superfly.web.mvc.model;

public class CheckPasswordResponse {
    private final String  username;
    private final String  sessionToken;
    private final boolean otpRequired;

    public CheckPasswordResponse(String username, String sessionToken, boolean otpRequired) {
        this.username = username;
        this.sessionToken = sessionToken;
        this.otpRequired = otpRequired;
    }

    public String getUsername() {
        return username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public boolean isOtpRequired() {
        return otpRequired;
    }
}

