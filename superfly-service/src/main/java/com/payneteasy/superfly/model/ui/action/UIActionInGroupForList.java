package com.payneteasy.superfly.model.ui.action;

import javax.persistence.Column;
import java.io.Serializable;

public class UIActionInGroupForList implements Serializable {
    private long id;
    private String name;
    private String description;
    private boolean logAction;
    private String subsystemName;
    private boolean selected;
    private String groupName;
    private long groupId;

    @Column(name = "actn_id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "action_name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "action_description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String descroption) {
        this.description = descroption;
    }

    @Column(name = "log_action")
    public boolean isLogAction() {
        return logAction;
    }

    public void setLogAction(boolean logAction) {
        this.logAction = logAction;
    }

    @Column(name = "subsystem_name")
    public String getSubsystemName() {
        return subsystemName;
    }

    public void setSubsystemName(String subsystemName) {
        this.subsystemName = subsystemName;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
}
