package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.SubsystemToNotify;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForFilter;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;

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
     * @param subsystem
     *            subsystem to create
     * @return routine result
     */
    @AStoredProcedure(name = "ui_create_subsystem")
    RoutineResult createSubsystem(UISubsystem subsystem);

    /**
     * Saves a subsystem.
     *
     * @param subsystem    subsystem to save
     * @return routine result
     */
    @AStoredProcedure(name = "ui_edit_subsystem_properties")
    RoutineResult updateSubsystem(UISubsystem subsystem);

    /**
     * Returns a subsystem by its ID.
     *
     * @param subsystemId    ID of the subsystem
     * @return subsystem or null if no such subsystem
     */
    @AStoredProcedure(name = "ui_get_subsystem")
    UISubsystem getSubsystem(long subsystemId);

    /**
     * Returns a subsystem by its name.
     *
     * @param subsystemName    name of the subsystem
     * @return subsystem or null if no such subsystem
     */
    @AStoredProcedure(name = "ui_get_subsystem_by_name")
    UISubsystem getSubsystemByName(String subsystemName);

    /**
     * Deletes a subsystem.
     *
     * @param subsystemId
     *            ID of a subsystem to delete
     * @return routine result
     */
    @AStoredProcedure(name = "ui_delete_subsystem")
    RoutineResult deleteSubsystem(long subsystemId);

    /**
     * Returns a list of all subsystems for a subsystem-based filter.
     *
     * @return subsystems
     */
    @AStoredProcedure(name = "ui_filter_subsystems")
    List<UISubsystemForFilter> getSubsystemsForFilter();

    /**
     * Returns a list of subsystems which allow to list users.
     *
     * @return subsystems
     */
    @AStoredProcedure(name = "get_subsystems_allowing_to_list_users")
    List<SubsystemToNotify> getSubsystemsAllowingToListUsers();

    /**
     * Tries to obtain a subsystem token. If user identified
     * by that SSO session can login to the requested subsystem,
     * subsystem token is returned.
     * If user cannot login to that subsystem, null is returned
     *
     * @param ssoSessionId          ID of SSO session
     * @param subsystemIdentifier   name of the subsystem
     * @return subsystem token or null
     */
    @AStoredProcedure(name = "create_subsystem_token_if_can_login")
    SubsystemTokenData issueSubsystemTokenIfCanLogin(long ssoSessionId,
            String subsystemIdentifier, String uniqueToken);
}
