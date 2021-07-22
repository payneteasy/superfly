package com.payneteasy.superfly.api;

public enum OTPType {
    NONE("none"),
    GOOGLE_AUTH("google_auth");

    private String code;

    OTPType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static OTPType fromCode(String type) {
        if (type == null) {
            return NONE;
        }
        if (type.equalsIgnoreCase(GOOGLE_AUTH.code)) {
            return GOOGLE_AUTH;
        }
        return NONE;
    }

    public static OTPType strictFromCode(String type) {
        if (type == null) {
            return null;
        }
        switch (type.toLowerCase().trim()) {
            case "google_auth":
                return GOOGLE_AUTH;
            case "none":
                return NONE;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}
