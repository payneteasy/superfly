package com.payneteasy.superfly.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

public class UserWithActions implements Serializable {
    private static final long serialVersionUID = -5266387450864343102L;

    private String username;
    private String email;
    private List<AuthAction> actions;

    @Column(name = "user_email")
    public final String getEmail() {
        return email;
    }

    public final void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "user_name")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @OneToMany
    @JoinColumn(table = "action")
    public List<AuthAction> getActions() {
        return actions;
    }

    public void setActions(List<AuthAction> actions) {
        this.actions = actions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((username == null) ? 0 : username.hashCode());
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
        UserWithActions other = (UserWithActions) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(username).append(" (").append(email).append(") ");
        buf.append(actions);
        return buf.toString();
    }
}
