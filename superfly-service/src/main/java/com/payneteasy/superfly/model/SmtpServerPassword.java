package com.payneteasy.superfly.model;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * A stored SMTP server password as it is in the database (plain text until encrypted).
 */
public class SmtpServerPassword implements Serializable {
    private Long   id;
    private String name;
    private String password;

    @Column(name = "ssrv_id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "server_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "SmtpServerPassword{id=" + id + ", name=" + name + '}';
    }
}
