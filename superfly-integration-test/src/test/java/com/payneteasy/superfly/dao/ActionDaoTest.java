package com.payneteasy.superfly.dao;

import java.util.ArrayList;
import java.util.List;

import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.utils.StringUtils;

public class ActionDaoTest extends AbstractDaoTest {
	private ActionDao actionDao;

	public void setActionDao(ActionDao actionDao) {
		this.actionDao = actionDao;
	}

	public void testGetActionsForfilter() {
        List<UIActionForFilter> actions = actionDao.getActionsForFilter(null, null, 0, Integer.MAX_VALUE);
        assertNotNull("List of actions must not be null", actions);
		assertTrue("List of actions cannot be empty", actions.size() > 0);
	}

	public void testSaveActions() {
		List<ActionToSave> actions = new ArrayList<ActionToSave>();
		ActionToSave action;
		action = new ActionToSave();
		action.setName("action1");
		action.setDescription("description1");
		actions.add(action);
		action = new ActionToSave();
		action.setName("action2");
		action.setDescription("description2");
		actions.add(action);
		actionDao.saveActions("test1", actions);
	}

	public void testGetActionForList() {
		List<UIActionForList> actionList = actionDao.getActions(0, 10, 1,
				"asc", null, null, "1,2");
		assertTrue("Action list should not be empty", actionList.size() > 0);
	}

	public void testGetActionCount() {
		int count = actionDao.getActionCount(null, null, null);
		assertTrue("Must get some action", count > 0);
		actionDao.getActionCount("someActionName", "someActionDescription",
				"1,2");
	}

	public void testchangeActionsLogLevel() {
		List<Long> logLevelsOn = new ArrayList<Long>();
		Long logLevelOn;
		logLevelOn = new Long(1);
		logLevelsOn.add(logLevelOn);
		logLevelOn = new Long(2);
		logLevelsOn.add(logLevelOn);
		List<Long> logLevelsOff = new ArrayList<Long>();
		Long logLevelOff;
		logLevelOff = new Long(3);
		logLevelsOff.add(logLevelOff);
		actionDao.changeActionsLogLevel(StringUtils
				.collectionToCommaDelimitedString(logLevelsOn), StringUtils
				.collectionToCommaDelimitedString(logLevelsOff));
	}
}
