package com.payneteasy.superfly.web.mvc.model;

public class CheckOtpRequest {
    private String username;
    private String otpEncrypted;
    private String sessionToken;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOtpEncrypted() {
        return otpEncrypted;
    }

    public void setOtpEncrypted(String otpEncrypted) {
        this.otpEncrypted = otpEncrypted;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}

