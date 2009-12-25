package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.payneteasy.superfly.dao.GroupDao;
import com.payneteasy.superfly.model.ui.UIGroup;
import com.payneteasy.superfly.model.ui.UIGroupForList;
import com.payneteasy.superfly.service.*;

@Transactional
public class GroupServiceImpl implements GroupService {
	private GroupDao groupDao;

	@Required
	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	public List<UIGroupForList> getGroups() {
		return this.groupDao.getGroups(0, 10, 1, "asc", null, null);
	}

	public void createGroup(UIGroup group) {
		groupDao.createGroup(group);
		
	}

	public void deleteGorup(long id) {
		groupDao.deleteGorup(id);
		
	}

}
