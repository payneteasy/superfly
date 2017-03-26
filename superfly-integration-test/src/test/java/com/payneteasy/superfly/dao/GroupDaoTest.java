package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.group.UICloneGroupRequest;
import com.payneteasy.superfly.model.ui.group.UIGroup;
import com.payneteasy.superfly.model.ui.group.UIGroupForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class GroupDaoTest extends AbstractDaoTest {
    private GroupDao groupDao;
    private SubsystemDao subsystemDao;
    
    private static boolean created = false;
    private static long subsystemId;

    @Autowired
    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    @Autowired
    public void setSubsystemDao(SubsystemDao subsystemDao) {
        this.subsystemDao = subsystemDao;
    }

    @Before
    public void setUp() throws Exception {
        if (!created) {
            UISubsystem subsystem = new UISubsystem();
            subsystem.setName("system-for-groups-1");
            subsystem.setCallbackUrl("http://localhost");
            subsystem.setTitle("Subsystem");
            subsystem.setSubsystemUrl("url");
            subsystem.setLandingUrl("url");
            subsystemDao.createSubsystem(subsystem);
            subsystemId = subsystem.getId();

            UIGroup group = new UIGroup();
            group.setName("group1");
            group.setSubsystemId(subsystemId);
            groupDao.createGroup(group);

            created = true;
        }
    }

    @Test
    public void testGetGroupsForSubsystems() {
        List<UIGroupForList> groupList = groupDao.getGroups(0, 10, 1, "asc", null, "1,2," + subsystemId);
        assertTrue("Group list should not be empty", groupList.size() > 0);
    }

    @Test
    public void testGetGroupCount(){
        long count = groupDao.getGroupsCount(null, null);
        assertTrue("Must get some group", count > 0);
    }

    @Test
    public void testCreateGetDeleteUpdateGroup(){
        UIGroup group = new UIGroup();
        group.setName("test");
        group.setSubsystemId(1L);
        RoutineResult routineResult =  groupDao.createGroup(group);
        assertRoutineResult(routineResult);
        groupDao.updateGroup(group.getId(), "test_updated");
        assertEquals(groupDao.getGroupById(group.getId()).getName(), "test_updated");
        routineResult = groupDao.deleteGroup(group.getId());
        assertRoutineResult(routineResult);
    }

    private long getAnyGroupId(){
        List<UIGroupForList> list = groupDao.getGroups(0, 1, 1, "asc", null, null);
        return list.get(0).getId();
    }

    @Test
    public void testChangeGroupActions() {
        long groupId = getAnyGroupId();
        assertRoutineResult(groupDao.changeGroupActions(groupId, "1,2,3", "4,5,6"));
    }

    @Test
    public void testGetAllGroupActions() {
        groupDao.getAllGroupMappedActions(0, 10, 1, "asc", getAnyGroupId(), null);
        groupDao.getAllGroupMappedActions(0, 10, 1, "asc", getAnyGroupId(), "dmi");
    }

    @Test
    public void testGetAllGroupActionsCount() {
        groupDao.getAllGroupMappedActionsCount(getAnyGroupId(), null);
        groupDao.getAllGroupMappedActionsCount(getAnyGroupId(), "dmi");
    }

    @Test
    public void testCloneGroup() {
        long groupId = getAnyGroupId();
        UICloneGroupRequest request = new UICloneGroupRequest();
        request.setNewGroupName("newgroup");
        request.setSourceGroupId(groupId);
        RoutineResult result = groupDao.cloneGroup(request);
        assertRoutineResult(result);

        UIGroup newGroup = groupDao.getGroupById(request.getId());
        assertNotNull("Group must be cloned", newGroup);
    }

    @Test
    public void testGetAllGroupUnMappedActions(){
        groupDao.getAllGroupUnMappedActions(0, 10, 1, "asc", getAnyGroupId(), null);
    }
}
