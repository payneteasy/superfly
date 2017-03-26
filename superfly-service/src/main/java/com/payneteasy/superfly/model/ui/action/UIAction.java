package com.payneteasy.superfly.model.ui.action;

import java.io.Serializable;

import javax.persistence.Column;

public class UIAction implements Serializable {
    private long actionId;
    private long subsystemId;
    private String actionName;
    private String subsystemName;
    private String actionDescription;
    @Column(name = "actn_id")
    public long getActionId() {
        return actionId;
    }
    public void setActionId(long actionId) {
        this.actionId = actionId;
    }
    @Column(name = "action_name")
    public String getActionName() {
        return actionName;
    }
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    @Column(name = "subsystem_name")
    public String getSubsystemName() {
        return subsystemName;
    }
    public void setSubsystemName(String subsystemName) {
        this.subsystemName = subsystemName;
    }
    @Column(name = "action_description")
    public String getActionDescription() {
        return actionDescription;
    }
    public void setActionDescription(String descriptionAction) {
        this.actionDescription = descriptionAction;
    }
    @Column(name = "subsystem_ssys_id")
    public long getSubsystemId() {
        return subsystemId;
    }
    public void setSubsystemId(long subsystemId) {
        this.subsystemId = subsystemId;
    }

}
