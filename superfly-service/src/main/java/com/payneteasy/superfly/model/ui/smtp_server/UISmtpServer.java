package com.payneteasy.superfly.model.ui.smtp_server;

import javax.persistence.Column;
import java.util.Objects;

/**
 * SMTP server with its password; the list representation has no password.
 *
 * @author rpuch
 */
public class UISmtpServer extends AbstractSmtpServer {
    private String password;

    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void validate() {
        Objects.requireNonNull(getUsername(), "'username' cannot be empty");
        Objects.requireNonNull(password, "'password' cannot be empty");
        Objects.requireNonNull(getHost(), "'host' is empty");

        if (getHost().isBlank()) {
            throw new IllegalArgumentException("'host' cannot be blank");
        }
        if (getUsername().isBlank()) {
            throw new IllegalArgumentException("'username' cannot be blank");
        }
        if (password.isBlank()) {
            throw new IllegalArgumentException("'password' cannot be blank");
        }
    }
}
