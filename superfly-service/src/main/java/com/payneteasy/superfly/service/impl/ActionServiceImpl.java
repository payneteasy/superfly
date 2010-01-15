package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.dao.ActionDao;
import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.model.ui.action.UIAction;
import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.service.ActionService;
import com.payneteasy.superfly.utils.StringUtils;

public class ActionServiceImpl implements ActionService {
	private ActionDao actionDao;

	@Required
	public void setActionDao(ActionDao actionDao) {
		this.actionDao = actionDao;
	}

	public void changeActionsLogLevel(List<Long> actnListLogOn,
			List<Long> actnListLogOff) {
		this.actionDao.changeActionsLogLevel(StringUtils
				.collectionToCommaDelimitedString(actnListLogOn), StringUtils
				.collectionToCommaDelimitedString(actnListLogOff));
	}

	public int getActionCount(String actionName, String description,
			List<Long> subsystemIds) {
		return actionDao.getActionCount(actionName, description, StringUtils
				.collectionToCommaDelimitedString(subsystemIds));
	}

	public List<UIActionForList> getActions(int startFrom, int recordsCount,
			int orderFieldNumber, boolean asc, String actionNamePrefix,
			String description, List<Long> subsystemIds) {
		return actionDao.getActions(startFrom, recordsCount, orderFieldNumber,
				asc ? DaoConstants.ASC : DaoConstants.DESC, actionNamePrefix, description, StringUtils
						.collectionToCommaDelimitedString(subsystemIds));
	}

	public List<UIActionForFilter> getActionForFilter() {
		return actionDao.getActionsForFilter(null, null, 0, Integer.MAX_VALUE);
	}

	public void copyActionProperties(long actionId, long actionIdCopy,
			boolean userPrivileges) {
		actionDao.copyActionProperties(actionId, actionIdCopy, userPrivileges);
	}

	public UIAction getAction(long actionId) {
		return actionDao.getAction(actionId);
	}
}
