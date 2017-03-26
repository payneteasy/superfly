package com.payneteasy.superfly.model.ui.role;

import java.io.Serializable;

import javax.persistence.Column;

/**
 * Role representation for a check-box.
 * 
 * @author Roman Puchkovskiy
 */
public class UIRoleForCheckbox implements Serializable {
    private long id;
    private String subsystemName;
    private String roleName;
    private String mappingStatus;
    private boolean mapped;

    @Column(name = "role_id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "subsystem_name")
    public String getSubsystemName() {
        return subsystemName;
    }

    public void setSubsystemName(String subsystemName) {
        this.subsystemName = subsystemName;
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
        UIRoleForCheckbox other = (UIRoleForCheckbox) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
