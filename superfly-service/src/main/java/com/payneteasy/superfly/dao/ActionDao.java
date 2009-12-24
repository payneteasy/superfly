package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.ActionToSave;

public interface ActionDao {
	@AStoredProcedure(name = "save_actions")
	void saveActions(String subsystemIdentifier, List<ActionToSave> actions);
}
