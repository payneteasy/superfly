package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.ui.UIGroupForList;

public interface GroupDao {

	@AStoredProcedure(name = "ui_get_groups_list")
	List<UIGroupForList> getGroups();
}
