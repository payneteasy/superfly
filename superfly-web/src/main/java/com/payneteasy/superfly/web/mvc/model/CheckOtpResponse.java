package com.payneteasy.superfly.web.mvc.model;

public class CheckOtpResponse {
    private String username;
    private String sessionToken;
    private String result;

    public CheckOtpResponse(String username, String sessionToken, String result) {
        this.username = username;
        this.sessionToken = sessionToken;
        this.result = result;
    }

    public String getUsername() {
        return username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getResult() {
        return result;
    }
}

