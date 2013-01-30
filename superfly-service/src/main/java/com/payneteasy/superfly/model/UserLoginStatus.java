package com.payneteasy.superfly.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rpuch
 */
public enum UserLoginStatus {
    SUCCESS("Y"), FAILED("N"), TEMP_PASSWORD("T");

    private final String dbStatus;

    private static final Map<String, UserLoginStatus> STATUSES = Collections.unmodifiableMap(new HashMap<String, UserLoginStatus>(){{
        for (UserLoginStatus s : UserLoginStatus.values()) {
            put(s.getDbStatus(), s);
        }
    }});

    private UserLoginStatus(String dbStatus) {
        this.dbStatus = dbStatus;
    }

    public String getDbStatus() {
        return dbStatus;
    }

    public static UserLoginStatus findByDbStatus(String status) {
        UserLoginStatus result = STATUSES.get(status);
        if (result == null) {
            throw new IllegalArgumentException("Cannot find login status by " + status);
        }
        return result;
    }
}
