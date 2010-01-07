package com.payneteasy.superfly.model.ui.action;

import java.io.Serializable;

import javax.persistence.Column;

public class UIActionForFilter implements Serializable {
	private long id;
	private String subsystemName;
	private String actionName;
	
	@Column(name = "actn_id")
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@Column(name = "subsystem_name")
	public String getSubsystemName() {
		return subsystemName;
	}
	public void setSubsystemName(String subsystemName) {
		this.subsystemName = subsystemName;
	}
	@Column(name = "action_name")
	public String getActionName() {
		return actionName;
	}
	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
}
