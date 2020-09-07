package com.payneteasy.superfly.model.ui.session;

import java.io.Serializable;

import javax.persistence.Column;

public class UISession implements Serializable {
    private Long id;
    private Long userId;
    private String username;
    private String callbackInformation;
    private boolean sendCallbacks;

    @Column(name = "sess_id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "user_id")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Column(name = "user_name")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "callback_information")
    public String getCallbackInformation() {
        return callbackInformation;
    }

    public void setCallbackInformation(String callbackInformation) {
        this.callbackInformation = callbackInformation;
    }

    @Column(name = "send_callbacks")
    public boolean isSendCallbacks() {
        return sendCallbacks;
    }

    public void setSendCallbacks(boolean sendCallbacks) {
        this.sendCallbacks = sendCallbacks;
    }
}
