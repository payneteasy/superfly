package com.payneteasy.superfly.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

public class AuthRole implements Serializable {
    private String roleName;
    private List<AuthAction> actions = new ArrayList<>();

    public AuthRole() {
    }

    public AuthRole(String roleName) {
        this.roleName = roleName;
    }

    @Column(name = "principal_name")
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
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
                + ((roleName == null) ? 0 : roleName.hashCode());
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
        AuthRole other = (AuthRole) obj;
        if (roleName == null) {
            if (other.roleName != null)
                return false;
        } else if (!roleName.equals(other.roleName))
            return false;
        return true;
    }
}
