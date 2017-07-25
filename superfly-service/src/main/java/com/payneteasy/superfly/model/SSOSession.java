package com.payneteasy.superfly.model;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * @author rpuch
 */
public class SSOSession implements Serializable {
    private long id;
    private String identifier;

    public SSOSession() {
    }

    public SSOSession(long id, String identifier) {
        this.id = id;
        this.identifier = identifier;
    }

    @Column(name = "sso_session_id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "identifier")
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return "SSOSession{" +
                "id=" + id +
                ", identifier='" + identifier + '\'' +
                '}';
    }
}
