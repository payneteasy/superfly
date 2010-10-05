package com.payneteasy.superfly.model.ui.user;

import javax.persistence.Column;

public class UIUserForCreate extends UIUser {
	private Long roleId;

	@Column(name = "role_id")
	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

}
