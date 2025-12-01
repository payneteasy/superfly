package com.payneteasy.superfly.web.mvc.model;

public class ErrorResponse {
    private String type;
    private String title;
    private String detail;
    private String errorId;

    public ErrorResponse(String type, String title, String detail) {
        this.type = type;
        this.title = title;
        this.detail = detail;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public String getErrorId() {
        return errorId;
    }

    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }
}

