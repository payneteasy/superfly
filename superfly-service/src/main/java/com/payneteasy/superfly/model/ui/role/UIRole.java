package com.payneteasy.superfly.model.ui.role;

import java.io.Serializable;

import javax.persistence.Column;

public class UIRole implements Serializable {
	private long roleId;
	private String roleName;
	private String principalName;
	private long subsystemId;
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
	 @Column(name="principal_name")
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
