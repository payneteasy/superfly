package com.payneteasy.superfly.web.wicket.page.group.wizard;

import java.io.Serializable;
import java.util.List;

import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;

public class GroupWizardModel implements Serializable {
	private static final long serialVersionUID = 1L;
	private String groupName;
	private UISubsystemForFilter groupSubsystem;
	private List<UIActionForList> actions;
	
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public UISubsystemForFilter getGroupSubsystem() {
		return groupSubsystem;
	}
	public void setGroupSubsystem(UISubsystemForFilter groupSubsystem) {
		this.groupSubsystem = groupSubsystem;
	}
	public List<UIActionForList> getActions() {
		return actions;
	}
	public void setActions(List<UIActionForList> actions) {
		this.actions = actions;
	}

}
