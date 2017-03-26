package com.payneteasy.superfly.model.ui.action;

import java.io.Serializable;

import javax.persistence.Column;

import com.payneteasy.superfly.service.mapping.MappingService;

public class UIActionForCheckboxForRole implements Serializable, MappingService {
    private long actionId;
    private long roleId;
    private String subsystemName;
    private String actionName;
    private String roleName;
    private String mappingStatus;

    @Column(name = "actn_id")
    public long getActionId() {
        return actionId;
    }
    public void setActionId(long actionId) {
        this.actionId = actionId;
    }
    @Column(name = "role_id")
    public long getRoleId() {
        return roleId;
    }
    public void setRoleId(long roleId) {
        this.roleId = roleId;
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
    @Column(name = "role_name")
    public String getRoleName() {
        return roleName;
    }
    public void setRoleName(String roleName) {
        this.roleName = roleName;
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

    public long getItemId() {
        return actionId;
    }
    public String getItemName() {
        return actionName;
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (actionId ^ (actionId >>> 32));
        result = prime * result + ((actionName == null) ? 0 : actionName.hashCode());
        result = prime * result + ((mappingStatus == null) ? 0 : mappingStatus.hashCode());
        result = prime * result + (int) (roleId ^ (roleId >>> 32));
        result = prime * result + ((roleName == null) ? 0 : roleName.hashCode());
        result = prime * result + ((subsystemName == null) ? 0 : subsystemName.hashCode());
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
        UIActionForCheckboxForRole other = (UIActionForCheckboxForRole) obj;
        if (actionId != other.actionId)
            return false;
        if (actionName == null) {
            if (other.actionName != null)
                return false;
        } else if (!actionName.equals(other.actionName))
            return false;
        if (mappingStatus == null) {
            if (other.mappingStatus != null)
                return false;
        } else if (!mappingStatus.equals(other.mappingStatus))
            return false;
        if (roleId != other.roleId)
            return false;
        if (roleName == null) {
            if (other.roleName != null)
                return false;
        } else if (!roleName.equals(other.roleName))
            return false;
        if (subsystemName == null) {
            if (other.subsystemName != null)
                return false;
        } else if (!subsystemName.equals(other.subsystemName))
            return false;
        return true;
    }

}
