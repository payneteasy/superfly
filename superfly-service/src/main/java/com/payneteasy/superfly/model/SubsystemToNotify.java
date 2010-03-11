package com.payneteasy.superfly.model;

import java.io.Serializable;

import javax.persistence.Column;

public class SubsystemToNotify implements Serializable {
	private Long id;
	private String callbackInformation;

	@Column(name = "ssys_id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "callback_information")
	public String getCallbackInformation() {
		return callbackInformation;
	}

	public void setCallbackInformation(String callbackInformation) {
		this.callbackInformation = callbackInformation;
	}
}
