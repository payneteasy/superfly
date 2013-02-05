package com.payneteasy.superfly.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.role.UIRoleForFilter;
import com.payneteasy.superfly.model.ui.role.UIRoleForList;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;

public class RoleDaoTest extends AbstractDaoTest {
	private RoleDao roleDao;
	private SubsystemDao subsystemDao;
	
	private static boolean created = false;

    @Autowired
	public void setRoleDao(RoleDao roleDao) {
		this.roleDao = roleDao;
	}

    @Autowired
    public void setSubsystemDao(SubsystemDao subsystemDao) {
		this.subsystemDao = subsystemDao;
	}

	public void setUp() throws Exception {
    	super.setUp();
    	
    	if (!created) {
    		UISubsystem subsystem = new UISubsystem();
    		subsystem.setName("subsystem-for-role-1");
    		subsystem.setCallbackUrl("no-callback");
            subsystem.setTitle("The Subsystem");
            subsystem.setSubsystemUrl("subsystem-for-role-1-url");
            subsystem.setLandingUrl("subsystem-for-role-1-url");
    		subsystemDao.createSubsystem(subsystem);
    		
	    	UIRole role = new UIRole();
	    	role.setRoleName("role1");
	    	role.setPrincipalName("role1");
	    	role.setSubsystemId(subsystem.getId());
	    	roleDao.createRole(role);
	    	
	    	created = true;
    	}
    }
    
	public void testGetAllRoleActions() {
		roleDao.getAllRoleActions(0, 10, 1, "asc", getAnyRoleId(), null);
	}

	public void testGetAllRoleActionsCount() {
		roleDao.getAllRoleActionsCount(getAnyRoleId(), null);
	}
	
	public void testGetMappedRoleActions() {
		roleDao.getMappedRoleActions(0, 10, 1, "asc", getAnyRoleId(), null);
		roleDao.getMappedRoleActions(0, 10, 1, "asc", getAnyRoleId(), "dmi");
	}
    public void testGetUnMappedRoleActions(){
    	roleDao.getUnMappedRoleActions(0, 10, 1, "asc", getAnyRoleId(), null);
    	roleDao.getUnMappedRoleActions(0, 10, 1, "asc", getAnyRoleId(), "dsf");
    }
	public void testGetMappedRoleActionsCount() {
		roleDao.getMappedRoleActionsCount(getAnyRoleId(), null);
		roleDao.getMappedRoleActionsCount(getAnyRoleId(), "dmi");
	}

	public void testChangeRoleActions() {
       long roleId = getAnyRoleId();
       roleDao.changeRoleActions(roleId, "1,2,3", "4,5,6");
       roleDao.changeRoleActions(roleId, null, "");
       roleDao.changeRoleActions(roleId, "", null);
	}

	public void testGetAllRoleGroups() {
		roleDao.getAllRoleGroups(0, 10, 1, "asc", getAnyRoleId());
	}

	public void testUpdateRole() {
		UIRole role = getAnyRole();
		role.setPrincipalName("principalNameTest");
		RoutineResult result = roleDao.updateRole(role);
		assertRoutineResult(result);
	}

	public void testChangeRoleGroups() {
		long roleId = getAnyRoleId();
		roleDao.changeRoleGroups(roleId, "1,2,3", "4,5,6");
		roleDao.changeRoleGroups(roleId, null, "");
		roleDao.changeRoleGroups(roleId, "", null);
	}

	private UIRole getAnyRole() {
		long roleId = getAnyRoleId();
		UIRole role = roleDao.getRole(roleId);
		return role;
	}

	public void testGetRolesForFilter() {
		List<UIRoleForFilter> roles = roleDao.getRolesForFilter(null, null, 0,
				Integer.MAX_VALUE);
		assertNotNull("List of roles must not be null", roles);
		assertTrue("List of roles cannot be empty", roles.size() > 0);
	}

	public void testGetRolesForList() {
		List<UIRoleForList> roles;
		roles = roleDao.getRoles(0, 10, DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
				"asc", null, null);
		assertNotNull("List cannot be null", roles);
		roles = roleDao.getRoles(0, 10, DaoConstants.DEFAULT_SORT_FIELD_NUMBER,
				"asc", "someRoleName", "1,2");
		assertNotNull("List cannot be null", roles);
	}

	public void testGetRoleCount() {
		int count = roleDao.getRoleCount(null, null);
		assertTrue("Must get some roles", count > 0);
		roleDao.getRoleCount("someRoleName", "1,2");
	}
	
    public void testGetAllRoleGroupsCount(){
    	roleDao.getAllRoleGroupsCount(1);
    }
    
	private long getAnyRoleId() {
		List<UIRoleForList> roles = roleDao
				.getRoles(0, 1, 1, "asc", null, null);
		UIRoleForList roleForList = roles.get(0);
		long roleId = roleForList.getId();
		return roleId;
	}

	public void testDeleteRole() {
		long roleId = getAnyRoleId();
		RoutineResult result = roleDao.deleteRole(roleId);
		assertRoutineResult(result);
	}

	public void testCreateDeleteRole() {
		UIRole role = new UIRole();
		role.setRoleName("Test Role Name");
		role.setPrincipalName("Test Role Principal Name");
		role.setSubsystemId(1L);
		RoutineResult result = roleDao.createRole(role);
		assertRoutineResult(result);
		result = roleDao.deleteRole(role.getRoleId());
		assertRoutineResult(result);
	}
	
	public void testgetMappedRoleGroups(){
		roleDao.getMappedRoleGroups(0, 10, 1, "asc", getAnyRoleId());
	}
	
	public void testGetUnMappedRoleGroups(){
		roleDao.getUnMappedRoleGroups(0, 10, 1, "asc", getAnyRoleId());
	}
}
