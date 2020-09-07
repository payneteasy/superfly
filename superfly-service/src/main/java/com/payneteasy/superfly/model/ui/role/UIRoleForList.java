package com.payneteasy.superfly.model.ui.role;

import java.io.Serializable;

import javax.persistence.Column;

public class UIRoleForList implements Serializable {
    private long id;
    private String name;
    private String subsystem;
    private String principalName;
    private boolean selected;
    
    
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Column(name="principal_name")
    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    @Column(name = "role_id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "role_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "subsystem_name")
    public String getSubsystem() {
        return subsystem;
    }

    public void setSubsystem(String subsystem) {
        this.subsystem = subsystem;
    }

}
