package com.payneteasy.superfly.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.dao.ActionDao;
import com.payneteasy.superfly.dao.DaoConstants;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.service.ActionService;

public class ActionServiceImpl implements ActionService {
	private ActionDao actionDao;

	@Required
	public void setActionDao(ActionDao actionDao) {
		this.actionDao = actionDao;
	}

	public List<UIActionForList> getAction() {
		return this.actionDao.getActions(0, 10,
				DaoConstants.DEFAULT_SORT_FIELD_NUMBER, DaoConstants.ASC,
				null, null, null);
	}

}
