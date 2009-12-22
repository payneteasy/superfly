package com.payneteasy.superfly.model.ui;

import java.io.Serializable;

import javax.persistence.Column;

public class UISubsystem implements Serializable {
	private Long id;
	private String identifier;
	private String name;
	private String callbackInformation;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Column(name = "subsystemname_name")
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
