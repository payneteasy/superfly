package com.payneteasy.superfly.model.ui.user;

import javax.persistence.Column;

public class UIUserForCreate extends UIUser {
	private Long roleId;
	private String hotpSalt;
    private String isPasswordTemp;

	@Column(name = "role_id")
	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	@Column(name = "hotp_salt")
	public String getHotpSalt() {
		return hotpSalt;
	}

	public void setHotpSalt(String hotpSalt) {
		this.hotpSalt = hotpSalt;
	}


    @Column(name="is_password_temp")
    public String getIsPasswordTemp() {
        return isPasswordTemp;
    }

    public void setIsPasswordTemp(String isPasswordTemp) {
        this.isPasswordTemp = isPasswordTemp;
    }
}
