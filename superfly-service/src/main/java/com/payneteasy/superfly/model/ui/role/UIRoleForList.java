package com.payneteasy.superfly.model.ui.role;

import java.io.Serializable;

import javax.persistence.Column;

public class UIRoleForList implements Serializable {
	private long id;
	private String name;
	private String subsystem;

	@Column(name = "role_id")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "role_name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "subsystem_name")
	public String getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(String subsystem) {
		this.subsystem = subsystem;
	}

}
