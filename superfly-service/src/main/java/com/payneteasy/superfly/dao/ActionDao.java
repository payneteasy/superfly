package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.RoutineResult;

/**
 * DAO to work with actions through jdbc-proc.
 * 
 * @author Roman Puchkovskiy
 */
public interface ActionDao {
	/**
	 * Saves actions for the given subsystem. Any actions of the subsystem
	 * which are not contained in the supplied list are removed. Any new actions
	 * are added.
	 * 
	 * @param subsystemIdentifier	identifier of the subsystem to operate on
	 * @param actions				list of actions
	 * @return routine result
	 */
	@AStoredProcedure(name = "save_actions")
	RoutineResult saveActions(String subsystemIdentifier,
			List<ActionToSave> actions);
}
