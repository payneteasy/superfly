package com.payneteasy.superfly.web.security.securehandler;

public class AuthorizationBearer {

    public final String subsystem;
    public final String token;

    public AuthorizationBearer(String aSubsystem, String aToken) {
        subsystem = aSubsystem;
        token = aToken;
    }

    @Override
    public String toString() {
        return "AuthorizationBearer{" +
                "subsystem='" + subsystem + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
