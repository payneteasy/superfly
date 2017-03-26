package com.payneteasy.superfly.model.ui.action;

import java.io.Serializable;

import javax.persistence.Column;

import com.payneteasy.superfly.service.mapping.MappingService;

public class UIActionForCheckboxForGroup implements Serializable, MappingService {
    private long actionId;
    private long groupId;
    private String subsystemName;
    private String actionName;
    private String groupName;
    private String mappingStatus;

    @Column(name = "actn_id")
    public long getActionId() {
        return actionId;
    }
    public void setActionId(long actionId) {
        this.actionId = actionId;
    }
    @Column(name = "grop_id")
    public long getGroupId() {
        return groupId;
    }
    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
    @Column(name = "subsystem_name")
    public String getSubsystemName() {
        return subsystemName;
    }
    public void setSubsystemName(String subsystemName) {
        this.subsystemName = subsystemName;
    }
    @Column(name = "action_name")
    public String getActionName() {
        return actionName;
    }
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    @Column(name = "group_name")
    public String getGroupName() {
        return groupName;
    }
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    @Column(name = "mapping_status")
    public String getMappingStatus() {
        return mappingStatus;
    }
    public void setMappingStatus(String mappingStatus) {
        this.mappingStatus = mappingStatus;
    }

    public boolean isMapped() {
        return "M".equalsIgnoreCase(mappingStatus);
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (actionId ^ (actionId >>> 32));
        result = prime * result + (int) (groupId ^ (groupId >>> 32));
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
        UIActionForCheckboxForGroup other = (UIActionForCheckboxForGroup) obj;
        if (actionId != other.actionId)
            return false;
        if (groupId != other.groupId)
            return false;
        return true;
    }
    public long getItemId() {
        return actionId;
    }
    public String getItemName() {
        return actionName;
    }
}
