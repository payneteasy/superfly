package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.ActionDao;
import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIAction;
import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.model.ui.action.UIActionWithGroupForList;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ActionServiceImpl implements ActionService {
    private ActionDao actionDao;

    @Autowired
    public void setActionDao(ActionDao actionDao) {
        this.actionDao = actionDao;
    }

    public void changeActionsLogLevel(List<Long> actnListLogOn,
                                      List<Long> actnListLogOff) {
        this.actionDao.changeActionsLogLevel(StringUtils
                .collectionToCommaDelimitedString(actnListLogOn), StringUtils
                .collectionToCommaDelimitedString(actnListLogOff));
    }

    public long getActionCount(String actionName, String description,
                               List<Long> subsystemIds) {
        return actionDao.getActionCount(actionName, description, StringUtils
                .collectionToCommaDelimitedString(subsystemIds));
    }

    public List<UIActionForList> getActions(long startFrom, long recordsCount,
                                            int orderFieldNumber, boolean asc, String actionNamePrefix,
                                            String description, List<Long> subsystemIds) {
        return actionDao.getActions(startFrom, recordsCount, orderFieldNumber,
                asc ? DaoConstants.ASC : DaoConstants.DESC, actionNamePrefix, description, StringUtils
                        .collectionToCommaDelimitedString(subsystemIds));
    }

    @Override
    public List<UIActionWithGroupForList> getActionsWithGroup(long startFrom, long recordsCount,
                                                              int orderFieldNumber, boolean asc, String actionNamePrefix,
                                                              String description, List<Long> subsystemIds) {
        return actionDao.getActionsWithGroup(startFrom, recordsCount, orderFieldNumber,
                asc ? DaoConstants.ASC : DaoConstants.DESC, actionNamePrefix, description, StringUtils
                        .collectionToCommaDelimitedString(subsystemIds));
    }

    public List<UIActionForFilter> getActionForFilter() {
        return actionDao.getActionsForFilter(null, null, 0, Integer.MAX_VALUE);
    }

    public RoutineResult copyActionProperties(long actionId, long templateActionId,
                                     boolean userPrivileges) {
        return actionDao.copyActionProperties(actionId, templateActionId, userPrivileges);
    }

    public UIAction getAction(long actionId) {
        return actionDao.getAction(actionId);
    }

    @Override
    public RoutineResult saveActions(String subsystemIdentifier, List<ActionToSave> actions) {
        return actionDao.saveActions(subsystemIdentifier, actions);
    }
}
