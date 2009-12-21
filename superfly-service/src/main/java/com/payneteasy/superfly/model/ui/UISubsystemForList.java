package com.payneteasy.superfly.model.ui;

import java.io.Serializable;

public class UISubsystemForList implements Serializable {
	private String identifier;
	private String name;
	private String callbackInformation;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCallbackInformation() {
		return callbackInformation;
	}

	public void setCallbackInformation(String callbackInformation) {
		this.callbackInformation = callbackInformation;
	}
}
