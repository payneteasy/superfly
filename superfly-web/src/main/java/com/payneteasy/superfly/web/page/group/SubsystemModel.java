package com.payneteasy.superfly.web.page.group;
import java.io.Serializable;

import com.payneteasy.superfly.model.ui.UISubsystemForFilter;

public class SubsystemModel implements Serializable{
	
	private UISubsystemForFilter uiSubsystemForFilter;

	public UISubsystemForFilter getUiSubsystemForFilter() {
		return uiSubsystemForFilter;
	}

	public void setUiSubsystemForFilter(UISubsystemForFilter uiSubsystemForFilter) {
		this.uiSubsystemForFilter = uiSubsystemForFilter;
	}
	
}
