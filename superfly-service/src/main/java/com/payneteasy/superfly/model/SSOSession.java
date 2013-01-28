package com.payneteasy.superfly.model;

import java.io.Serializable;

/**
 * @author rpuch
 */
public class SSOSession implements Serializable {
    private long id;

    public SSOSession() {
    }

    public SSOSession(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
