package com.payneteasy.superfly.model.ui.group;

import javax.persistence.Column;

public class UIGroupForView extends UIGroup {
    private String subsystemName;

    @Column(name = "subsystem_name")
    public String getSubsystemName() {
        return subsystemName;
    }

    public void setSubsystemName(String subsystemName) {
        this.subsystemName = subsystemName;
    }
}
