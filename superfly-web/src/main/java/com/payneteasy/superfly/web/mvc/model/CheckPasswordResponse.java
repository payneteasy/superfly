package com.payneteasy.superfly.web.mvc.model;

public class CheckPasswordResponse {
    private String username;
    private String sessionToken;

    public CheckPasswordResponse(String username, String sessionToken) {
        this.username = username;
        this.sessionToken = sessionToken;
    }

    public String getUsername() {
        return username;
    }

    public String getSessionToken() {
        return sessionToken;
    }
}

