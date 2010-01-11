package com.payneteasy.superfly.dao;

import java.util.ArrayList;
import java.util.List;

import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.group.UIGroupForList;
import com.payneteasy.superfly.utils.StringUtils;

public class GroupDaoTest extends AbstractDaoTest {
	private GroupDao groupDao;

	public void setGroupDao(GroupDao groupDao) {
		this.groupDao = groupDao;
	}

	
	public void testGetGroupsForSubsystems(){
		List<UIGroupForList> groupList = groupDao.getGroups(0, 10, 1, "asc", null, "1,2");
		assertTrue("Action list should not be empty", groupList.size() > 0);
	}
	
	public void testGetGroupCount(){		
		int count = groupDao.getGroupsCount(null, null);
		assertTrue("Must get some action", count > 0);
	}
	
	public void testCreateDeleteGroup(){
		UIGroup group = new UIGroup();
		group.setName("test001");
		group.setSubsystemId(1L);
		RoutineResult routineResult =  groupDao.createGroup(group);
		assertRoutineResult(routineResult);
		routineResult = groupDao.deleteGorup(group.getId());
		assertRoutineResult(routineResult);
	}
	/*public void testSaveActions() {
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
		groupDao.saveActions("test1", actions);
	}

	public void testGetActionForList() {
		List<UIActionForList> actionList = groupDao.getActions(0, 10, 1,
				"asc", null, null, "1,2");
		assertTrue("Action list should not be empty", actionList.size() > 0);
	}
	public void testGetActionCount(){
		int count = groupDao.getActionCount(null, null, null);
		assertTrue("Must get some action", count > 0);
		groupDao.getActionCount("someActionName", "someActionDescription", "1,2");
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
		logLevelOff =new Long(3);
		logLevelsOff.add(logLevelOff);
		groupDao.changeActionsLogLevel(StringUtils
				.collectionToCommaDelimitedString(logLevelsOn), StringUtils
				.collectionToCommaDelimitedString(logLevelsOff));
	}*/
}
