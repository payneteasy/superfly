package com.payneteasy.superfly.model.ui.action;

import java.io.Serializable;

import javax.persistence.Column;

public class UIActionForCheckboxForRole implements Serializable {
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (actionId ^ (actionId >>> 32));
		result = prime * result + (int) (roleId ^ (roleId >>> 32));
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
		if (roleId != other.roleId)
			return false;
		return true;
	}
}
