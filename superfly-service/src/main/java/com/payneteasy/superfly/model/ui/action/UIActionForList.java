package com.payneteasy.superfly.model.ui.action;

import java.io.Serializable;

import javax.persistence.Column;

public class UIActionForList implements Serializable {
	private long id;
	private String name;
	private String descroption;
	private boolean logAction;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@Column(name = "action_name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name = "action_description")
	public String getDescroption() {
		return descroption;
	}
	public void setDescroption(String descroption) {
		this.descroption = descroption;
	}
	@Column(name = "log_action")
	public boolean isLogAction() {
		return logAction;
	}
	public void setLogAction(boolean logAction) {
		this.logAction = logAction;
	}
	
}
