package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.group.UIGroupForList;

public interface GroupService {
	List<UIGroupForList> getGroups();
	List<UIGroupForList> getGroupsForSubsystems(int startFrom, int recordsCount,
			int orderFieldNumber, boolean orderType, String groupNamePrefix,
			List<Long> subsystemIds);
	void createGroup(UIGroup group);

	void deleteGroup(long id);
	
	int getGroupsCount(String groupName, List<Long> subsystemIds);

}
