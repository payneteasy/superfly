package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.ui.UISubsystem;
import com.payneteasy.superfly.model.ui.UISubsystemForList;

/**
 * DAO to work with subsystems.
 * 
 * @author Roman Puchkovskiy
 */
public interface SubsystemDao {
	
	/**
	 * Returns a list of registered subsystems.
	 * 
	 * @return subsystems
	 */
	@AStoredProcedure(name = "ui_get_subsystems_list")
	List<UISubsystemForList> getSubsystems();
	
	/**
	 * Creates a subsystem.
	 * 
	 * @param subsystem	subsystem to create
	 */
	@AStoredProcedure(name = "ui_create_subsystem")
	void createSubsystem(UISubsystem subsystem);
	
	/**
	 * Deletes a subsystem.
	 * 
	 * @param subsystemId	ID of a subsystem to delete
	 */
	@AStoredProcedure(name = "ui_delete_subsystem")
	void deleteSubsystem(long subsystemId);
}
