package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.group.UIGroupForList;

public interface GroupDao {

	@AStoredProcedure(name = "ui_get_groups_list")
	List<UIGroupForList> getGroups(int startFrom, int recordsCount,
			int orderFieldNumber, String orderType, String groupNamePrefix,
			String subsystemIds);

	@AStoredProcedure(name = "ui_create_group")
	RoutineResult createGroup(UIGroup group);

	@AStoredProcedure(name = "ui_delete_group")
	RoutineResult deleteGorup(long id);
}
