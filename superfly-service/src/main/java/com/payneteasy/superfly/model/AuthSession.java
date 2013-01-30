package com.payneteasy.superfly.model;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rpuch
 */
public class AuthSession {
    private String username;
    private Long sessionId;
    private List<AuthRole> roles = new ArrayList<AuthRole>();

    public AuthSession() {
    }

    public AuthSession(String username) {
        this.username = username;
    }

    public AuthSession(String username, Long sessionId) {
        this.username = username;
        this.sessionId = sessionId;
    }

    @Column(name = "username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column(name = "session_id")
   	public Long getSessionId() {
   		return sessionId;
   	}

   	public void setSessionId(Long sessionId) {
   		this.sessionId = sessionId;
   	}

    @OneToMany
    @JoinColumn(table = "role")
    public List<AuthRole> getRoles() {
        return roles;
    }

    public void setRoles(List<AuthRole> roles) {
        this.roles = roles;
    }
}
