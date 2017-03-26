package com.payneteasy.superfly.model.ui.group;

import java.io.Serializable;

import javax.persistence.Column;

import com.payneteasy.superfly.service.mapping.MappingService;

public class UIGroupForCheckbox implements Serializable, MappingService {
    private long groupId;
    private String subsystemName;
    private String groupName;
    private String mappingStatus;
    private boolean mapped;

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
        mapped = "M".equalsIgnoreCase(mappingStatus);
    }

    public boolean isMapped() {
        return mapped;
    }

    public void setMapped(boolean mapped) {
        this.mapped = mapped;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        UIGroupForCheckbox other = (UIGroupForCheckbox) obj;
        if (groupId != other.groupId)
            return false;
        return true;
    }

    public long getItemId() {
        return groupId;
    }

    public String getItemName() {
        return groupName;
    }


}
