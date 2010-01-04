package com.payneteasy.superfly.dao;

import java.util.ArrayList;
import java.util.List;

import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.ui.action.UIActionForList;

public class ActionDaoTest extends AbstractDaoTest {
	private ActionDao actionDao;

	public void setActionDao(ActionDao actionDao) {
		this.actionDao = actionDao;
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
	public void testGetActionForList(){
		List<UIActionForList> actionList = actionDao.getActions(0, 10, 1, "asc", null, null, "1,2");
		assertTrue("Action list should not be empty", actionList.size() > 0);
	}
}
