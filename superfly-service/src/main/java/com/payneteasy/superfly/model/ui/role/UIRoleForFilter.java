package com.payneteasy.superfly.model.ui.role;

import java.io.Serializable;

import javax.persistence.Column;

/**
 * Role to be shown in the drop-down list.
 * 
 * @author Roman Puchkovskiy
 */
public class UIRoleForFilter implements Serializable {
	private long id;
	private String subsystemName;
	private String roleName;

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
}
