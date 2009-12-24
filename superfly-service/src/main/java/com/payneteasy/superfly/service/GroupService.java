package com.payneteasy.superfly.service;

import java.util.List;

import com.payneteasy.superfly.model.ui.UIGroup;
import com.payneteasy.superfly.model.ui.UIGroupForList;

public interface GroupService {
	List<UIGroupForList> getGroups();

	void createGroup(UIGroup group);

	void deleteGorup(long id);
}
