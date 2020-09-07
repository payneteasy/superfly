package com.payneteasy.superfly.dao;

import java.util.List;

import com.googlecode.jdbcproc.daofactory.annotation.AStoredProcedure;
import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIAction;
import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;

/**
 * DAO to work with actions through jdbc-proc.
 * 
 * @author Roman Puchkovskiy
 */
public interface ActionDao {
    /**
     * Saves actions for the given subsystem. Any actions of the subsystem which
     * are not contained in the supplied list are removed. Any new actions are
     * added.
     *
     * @param subsystemIdentifier
     *            identifier of the subsystem to operate on
     * @param actions
     *            list of actions
     * @return routine result
     */
    @AStoredProcedure(name = "save_actions")
    RoutineResult saveActions(String subsystemIdentifier,
            List<ActionToSave> actions);

    @AStoredProcedure(name = "ui_get_actions_list")
    List<UIActionForList> getActions(long startFrom, long recordsCount,
            int orderFieldNumber, String orderType, String actionNamePrefix,
            String description, String subsystemIds);

    @AStoredProcedure(name = "ui_change_actions_log_level")
    void changeActionsLogLevel(String actnListLogOn, String actnListLogOff);

    @AStoredProcedure(name = "ui_get_actions_list_count")
    long getActionCount(String actionName, String description,
            String subsystemIds);

    @AStoredProcedure(name = "ui_filter_dyn_actions")
    List<UIActionForFilter> getActionsForFilter(String subsystemIds,
            String actionNamePrefix, int startFrom, int recordsCount);

    @AStoredProcedure(name = "ui_copy_action_properties")
    RoutineResult copyActionProperties(long actionId, long actionIdCopy,
            boolean userPrivileges);

    @AStoredProcedure(name = "ui_get_action")
    UIAction getAction(long actionId);
}
