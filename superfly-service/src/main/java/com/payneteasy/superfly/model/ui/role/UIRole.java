package com.payneteasy.superfly.model.ui.role;

import java.io.Serializable;

public class UIRole implements Serializable {
	private long roleId;
	private String roleName;
	private String principalName;
	private long subsystemId;

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getPrincipalName() {
		return principalName;
	}

	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}

	public long getSubsystemId() {
		return subsystemId;
	}

	public void setSubsystemId(long subsystemId) {
		this.subsystemId = subsystemId;
	}

}
