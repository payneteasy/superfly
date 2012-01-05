package com.payneteasy.superfly.model.ui.smtp_server;

import javax.persistence.Column;
import java.io.Serializable;

/**
 * @author rpuch
 */
public class UISmtpServerForFilter implements Serializable {
    private long id;
    private String name;

    @Column(name = "ssrv_id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "server_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
