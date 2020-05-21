package com.payneteasy.superfly.api;

public enum HOTPType {
    NONE("none", ""),
    HOTP("hotp", "ACTION_HOTP_TYPE"),
    GOOGLE_AUTH("ga", "ACTION_HOTP_GA");

    private String type;
    private String action;

    HOTPType(String type, String action) {
        this.type   = type;
        this.action = action;
    }

    public String type() {
        return type;
    }

    public String action() {
        return action;
    }

    public static HOTPType fromAction(String action) {
        if (action.equalsIgnoreCase(HOTPType.HOTP.action)) {
            return HOTPType.HOTP;
        }
        if (action.equalsIgnoreCase(HOTPType.GOOGLE_AUTH.action)) {
            return HOTPType.GOOGLE_AUTH;
        }
        return HOTPType.NONE;
    }

    public static HOTPType fromType(String type) {
        if (type.equalsIgnoreCase(HOTPType.HOTP.type)) {
            return HOTPType.HOTP;
        }
        if (type.equalsIgnoreCase(HOTPType.GOOGLE_AUTH.type)) {
            return HOTPType.GOOGLE_AUTH;
        }
        return HOTPType.NONE;
    }
}
