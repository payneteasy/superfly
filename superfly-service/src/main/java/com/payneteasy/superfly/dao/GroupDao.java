package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.ui.UIGroup;
import com.payneteasy.superfly.model.ui.UIGroupForList;

public interface GroupDao {

	@AStoredProcedure(name = "ui_get_groups_list")
	List<UIGroupForList> getGroups(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType, String groupName,
			String subsystemIds);

	@AStoredProcedure(name = "ui_create_group")
	void createGroup(UIGroup group);

	@AStoredProcedure(name = "ui_delete_group")
	void deleteGorup(long id);
}
