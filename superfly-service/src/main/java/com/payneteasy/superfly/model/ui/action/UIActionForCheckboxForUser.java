package com.payneteasy.superfly.model.ui.action;

import java.io.Serializable;

import javax.persistence.Column;

/**
 * Action representation for a check-box used to assign it to a user.
 * 
 * @author Roman Puchkovskiy
 */
public class UIActionForCheckboxForUser implements Serializable {
	private String subsystemName;
	private long actionId;
	private String actionName;
	private String mappingStatus;
	private long roleActionId;
	private long roleId;
	private String roleName;
	

	@Column(name = "subsystem_name")
	public String getSubsystemName() {
		return subsystemName;
	}

	public void setSubsystemName(String subsystemName) {
		this.subsystemName = subsystemName;
	}

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

	@Column(name = "ract_id")
	public long getRoleActionId() {
		return roleActionId;
	}

	public void setRoleActionId(long roleActionId) {
		this.roleActionId = roleActionId;
	}

	@Column(name = "role_id")
	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	@Column(name = "role_name")
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
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
		UIActionForCheckboxForUser other = (UIActionForCheckboxForUser) obj;
		if (actionId != other.actionId)
			return false;
		if (roleId != other.roleId)
			return false;
		return true;
	}
}
