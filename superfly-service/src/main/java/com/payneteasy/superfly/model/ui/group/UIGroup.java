package com.payneteasy.superfly.model.ui.group;

import java.io.Serializable;

import javax.persistence.Column;

public class UIGroup implements Serializable {
    private long id;
    private String name;
    private long subsystemId;

    @Column(name = "grop_id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    @Column(name = "group_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Column(name = "ssys_id")
    public long getSubsystemId() {
        return subsystemId;
    }

    public void setSubsystemId(long subsystemId) {
        this.subsystemId = subsystemId;
    }

    public String getLabel(){
        return name + " ["+id+"]";
    }

}
