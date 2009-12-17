package com.payneteasy.superfly.model;

import java.io.Serializable;

import javax.persistence.Column;

public class AuthAction implements Serializable {
	private String actionName;
	private boolean logAction;

	@Column(name = "action_name")
	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	@Column(name = "log_action")
	public boolean isLogAction() {
		return logAction;
	}

	public void setLogAction(boolean logAction) {
		this.logAction = logAction;
	}
}
