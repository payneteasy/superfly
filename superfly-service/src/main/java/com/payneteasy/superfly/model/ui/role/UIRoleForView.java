package com.payneteasy.superfly.model.ui.role;

import javax.persistence.Column;

public class UIRoleForView extends UIRole {
	private String subsystemName;

	@Column(name = "subsystem_name")
	public String getSubsystemName() {
		return subsystemName;
	}

	public void setSubsystemName(String subsystemName) {
		this.subsystemName = subsystemName;
	}
}
