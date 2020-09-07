package com.payneteasy.superfly.model.ui.role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import com.payneteasy.superfly.model.ui.action.UILinkedAction;

public class UIRoleWithActions implements Serializable {
    private String subsystemName;
    private long id;
    private String name;
    private List<UILinkedAction> actions = new ArrayList<UILinkedAction>();

    @Column(name = "subsystem_name")
    public String getSubsystemName() {
        return subsystemName;
    }

    public void setSubsystemName(String subsystemName) {
        this.subsystemName = subsystemName;
    }

    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany
    @JoinColumn(table = "action")
    public List<UILinkedAction> getActions() {
        return actions;
    }

    public void setActions(List<UILinkedAction> actions) {
        this.actions = actions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
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
        UIRoleWithActions other = (UIRoleWithActions) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
