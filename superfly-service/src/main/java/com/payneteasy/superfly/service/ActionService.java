package com.payneteasy.superfly.service;

import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIAction;
import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;

import java.util.List;

public interface ActionService {
    List<UIActionForList> getActions(long startFrom, long recordsCount,
            int orderFieldNumber, boolean asc, String actionNamePrefix,
            String description, List<Long> subsystemIds, boolean byGroup);

    void changeActionsLogLevel(List<Long> actnListLogOn,
            List<Long> actnListLogOff);

    long getActionCount(String actionName, String description,
            List<Long> subsystemIds);

    List<UIActionForFilter> getActionForFilter();
    void copyActionProperties(long actionId, long actionIdCopy,
            boolean userPrivileges);
    UIAction getAction(long actionId);

    RoutineResult saveActions(String subsystemIdentifier,
                              List<ActionToSave> actions);
}
