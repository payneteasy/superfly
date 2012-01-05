package com.payneteasy.superfly.model.ui.smtp_server;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Base for SMTP server representations.
 *
 * @author rpuch
 */
public abstract class AbstractSmtpServer implements Serializable {
    private Long id;
    private String name;
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String from;

    @Column(name = "ssrv_id")
    @Id
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

    @Column(name = "host")
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Column(name = "port")
    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Column(name = "username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "from_address")
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
