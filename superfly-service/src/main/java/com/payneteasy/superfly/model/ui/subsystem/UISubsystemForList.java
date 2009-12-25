package com.payneteasy.superfly.model.ui.subsystem;

import java.io.Serializable;

import javax.persistence.Column;

public class UISubsystemForList implements Serializable {
	private long id;
	private String identifier;
	private String name;
	private String callbackInformation;

	@Column(name = "ssys_id")
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Column(name = "subsystem_name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "callback_information")
	public String getCallbackInformation() {
		return callbackInformation;
	}

	public void setCallbackInformation(String callbackInformation) {
		this.callbackInformation = callbackInformation;
	}
}
