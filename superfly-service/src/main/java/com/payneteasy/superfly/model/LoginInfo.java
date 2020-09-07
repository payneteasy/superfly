package com.payneteasy.superfly.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

public class LoginInfo implements Serializable {
    private long sessionId;
    private List<AuthRole> roles = new ArrayList<AuthRole>();

    @Column(name = "session_id")
    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (sessionId ^ (sessionId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LoginInfo other = (LoginInfo) obj;
        if (sessionId != other.sessionId)
            return false;
        return true;
    }
}
