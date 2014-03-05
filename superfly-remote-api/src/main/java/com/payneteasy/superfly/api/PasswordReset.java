package com.payneteasy.superfly.api;

import java.io.Serializable;

/**
 * @author rpuch
 */
public class PasswordReset implements Serializable {
    private String username;
    private String password;
    private boolean sendByEmail;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSendByEmail() {
        return sendByEmail;
    }

    public void setSendByEmail(boolean sendByEmail) {
        this.sendByEmail = sendByEmail;
    }
}
