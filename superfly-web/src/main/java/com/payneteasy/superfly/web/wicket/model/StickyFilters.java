package com.payneteasy.superfly.web.wicket.model;

import java.io.Serializable;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;

/**
 * Model object which holds filters which are 'sticky'. A sticky filter is one
 * that is remembered for all screens when set on one of screens.
 * 
 * @author Roman Puchkovskiy
 */
public class StickyFilters implements Serializable {
	private UISubsystemForFilter subsystem;
	private String actionNameSubstring = "";
	
	public UISubsystemForFilter getSubsystem() {
		return subsystem;
	}

	public void setSubsystem(UISubsystemForFilter subsystem) {
		this.subsystem = subsystem;
	}

	public String getActionNameSubstring() {
		return actionNameSubstring;
	}

	public void setActionNameSubstring(String actionNameSubstring) {
		this.actionNameSubstring = actionNameSubstring;
	}
}
