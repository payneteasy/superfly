package com.payneteasy.superfly.model.ui.subsystem;

import java.io.Serializable;

import javax.persistence.Column;

public class UISubsystemForFilter implements Serializable {
	private long id;
	private String name;

	@Column(name = "ssys_id")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "subsystem_name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
