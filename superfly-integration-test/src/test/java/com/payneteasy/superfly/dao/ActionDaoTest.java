package com.payneteasy.superfly.dao;

import java.util.ArrayList;
import java.util.List;

import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.ui.action.UIActionForFilter;
import com.payneteasy.superfly.model.ui.action.UIActionForList;
import com.payneteasy.superfly.model.ui.action.UIActionWithGroupForList;
import com.payneteasy.superfly.utils.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ActionDaoTest extends AbstractDaoTest {
    private ActionDao actionDao;

    @Autowired
    public void setActionDao(ActionDao actionDao) {
        this.actionDao = actionDao;
    }

    @Test
    public void testCopyActionProperties(){
        actionDao.copyActionProperties(1, 2, true);
    }

    @Test
    public void testGetAction(){
        actionDao.getAction(1);
    }

    @Test
    public void testGetActionsForfilter() {
        List<UIActionForFilter> actions = actionDao.getActionsForFilter(null, null, 0, Integer.MAX_VALUE);
        Assert.assertNotNull("List of actions must not be null", actions);
        Assert.assertTrue("List of actions cannot be empty", actions.size() > 0);
    }

    @Test
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
        actionDao.saveActions("superfly-demo", actions);
    }

    @Test
    public void testGetActionForList() {
        List<UIActionForList> actionList = actionDao.getActions(0, 10, 1,
                "asc", null, null, "1,2");
        Assert.assertTrue("Action list should not be empty", actionList.size() > 0);
    }

    @Test
    public void testGetActionCount() {
        long count = actionDao.getActionCount(null, null, null);
        Assert.assertTrue("Must get some action", count > 0);
        actionDao.getActionCount("someActionName", "someActionDescription",
                "1,2");
    }

    @Test
    public void testGetActionsWithGroupIncludesActionWithoutGroup() {
        List<UIActionWithGroupForList> actions = actionDao.getActionsWithGroup(0,
                Integer.MAX_VALUE, 1, "asc", null, null, null);
        boolean ungroupedFound = false;
        for (UIActionWithGroupForList action : actions) {
            if (action.getGroupName() == null) {
                ungroupedFound = true;
                break;
            }
        }
        Assert.assertTrue("Actions not belonging to any group must be listed", ungroupedFound);
    }

    @Test
    public void testGetActionsWithGroupCountMatchesList() {
        List<UIActionWithGroupForList> actions = actionDao.getActionsWithGroup(0,
                Integer.MAX_VALUE, 1, "asc", null, null, null);
        Assert.assertEquals("Count must match the number of listed rows",
                actions.size(), actionDao.getActionsWithGroupCount(null, null, null));

        String namePart = actions.get(0).getName();
        List<UIActionWithGroupForList> filtered = actionDao.getActionsWithGroup(0,
                Integer.MAX_VALUE, 1, "asc", namePart, null, null);
        Assert.assertEquals("Count must honour the action name filter",
                filtered.size(), actionDao.getActionsWithGroupCount(namePart, null, null));
    }

    @Test
    public void testActionNameFilterIsEscaped() {
        Assert.assertEquals("Quote in a filter must not break the query", 0,
                actionDao.getActionsWithGroupCount("O'Brien", null, null));
        Assert.assertTrue("Quote in a filter must not break the query",
                actionDao.getActionsWithGroup(0, 10, 1, "asc", "O'Brien", null, null).isEmpty());
    }

    @Test
    public void testChangeActionsLogLevel() {
        List<Long> logLevelsOn = new ArrayList<Long>();
        Long logLevelOn;
        logLevelOn = (long) 1;
        logLevelsOn.add(logLevelOn);
        logLevelOn = (long) 2;
        logLevelsOn.add(logLevelOn);
        List<Long> logLevelsOff = new ArrayList<Long>();
        Long logLevelOff;
        logLevelOff = (long) 3;
        logLevelsOff.add(logLevelOff);
        actionDao.changeActionsLogLevel(StringUtils
                .collectionToCommaDelimitedString(logLevelsOn), StringUtils
                .collectionToCommaDelimitedString(logLevelsOff));
    }
}
