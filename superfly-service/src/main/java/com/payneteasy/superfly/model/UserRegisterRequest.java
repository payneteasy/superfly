package com.payneteasy.superfly.model;

import java.io.Serializable;

import javax.persistence.Column;

public class UserRegisterRequest implements Serializable {
	private static final long serialVersionUID = -4093140271801747768L;
	
	private long userid;
	private String username;
	private String password;
	private String email;
	private String subsystemName;
	private String principalNames;
	
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
	
	@Column(name = "subsystem_name")
	public String getSubsystemName() {
		return subsystemName;
	}

	public void setSubsystemName(String subsystemName) {
		this.subsystemName = subsystemName;
	}

	@Column(name = "principal_list")
	public String getPrincipalNames() {
		return principalNames;
	}

	public void setPrincipalNames(String principalNames) {
		this.principalNames = principalNames;
	}

}
