package com.payneteasy.superfly.model;

import java.io.Serializable;

import javax.persistence.Column;

public class RegisterUser implements Serializable {
	
    private long userid;
	private String username;
	private String password;
	private String email;
	private long subsystemId;
	private String principalName;
	
	@Column(name = "user_id")
	public long getUserid() {
		return userid;
	}

	public void setUserid(long userid) {
		this.userid = userid;
	}

	@Column(name = "user_name")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	@Column(name = "user_password")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	@Column(name = "user_email")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	@Column(name = "ssys_id")
	public long getSubsystemId() {
		return subsystemId;
	}

	public void setSubsystemId(long subsystemId) {
		this.subsystemId = subsystemId;
	}
	@Column(name = "principal_list")
	public String getPrincipalName() {
		return principalName;
	}

	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}

}
