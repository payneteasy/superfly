package com.payneteasy.superfly.model.ui.action;

import java.io.Serializable;

import javax.persistence.Column;

public class UILinkedAction implements Serializable {
    private long roleActionId;
    private long id;
    private String name;

    @Column(name = "ract_id")
    public long getRoleActionId() {
        return roleActionId;
    }

    public void setRoleActionId(long roleActionId) {
        this.roleActionId = roleActionId;
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
}
