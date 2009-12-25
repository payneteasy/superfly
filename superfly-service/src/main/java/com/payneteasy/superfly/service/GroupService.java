package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.group.UIGroupForList;

public interface GroupService {
	List<UIGroupForList> getGroups();

	void createGroup(UIGroup group);

	void deleteGorup(long id);
}
