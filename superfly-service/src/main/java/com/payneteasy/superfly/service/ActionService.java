package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.ui.action.UIAction;
import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;

public interface ActionService {
	List<UIActionForList> getActions(int startFrom, int recordsCount,
			int orderFieldNumber, boolean asc, String actionNamePrefix,
			String description, List<Long> subsystemIds);

	void changeActionsLogLevel(List<Long> actnListLogOn,
			List<Long> actnListLogOff);

	int getActionCount(String actionName, String description,
			List<Long> subsystemIds);
	
	List<UIActionForFilter> getActionForFilter();
	void copyActionProperties(long actionId, long actionIdCopy,
			boolean userPrivileges);
	UIAction getAction(long actionId);
}
