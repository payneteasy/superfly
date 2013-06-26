package com.payneteasy.superfly.service.impl;


import static org.easymock.EasyMock.anyLong;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

import java.util.Collections;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import com.payneteasy.superfly.dao.RoleDao;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.RoleService;

public class RoleServiceLoggingTest extends AbstractServiceLoggingTest {
	
	private RoleService roleService;
	private RoleDao roleDao;

    @Before
	public void setUp() {
		RoleServiceImpl service = new RoleServiceImpl();
		roleDao = EasyMock.createStrictMock(RoleDao.class);
		service.setRoleDao(roleDao);
		service.setNotificationService(TrivialProxyFactory.createProxy(NotificationService.class));
		service.setLoggerSink(loggerSink);
		roleService = service;
	}

    @Test
	public void testCreateRole() throws Exception {
		roleDao.createRole(anyObject(UIRole.class));
		EasyMock.expectLastCall().andReturn(okResult());
		loggerSink.info(anyObject(Logger.class), eq("CREATE_ROLE"), eq(true), eq("test-role in 1"));
		EasyMock.replay(loggerSink, roleDao);
		
		UIRole role = new UIRole();
		role.setRoleName("test-role");
		role.setSubsystemId(1L);
		roleService.createRole(role);
		
		EasyMock.verify(loggerSink);
	}

    @Test
	public void testCreateRoleFail() throws Exception {
		roleDao.createRole(anyObject(UIRole.class));
		EasyMock.expectLastCall().andReturn(failureResult());
		loggerSink.info(anyObject(Logger.class), eq("CREATE_ROLE"), eq(false), eq("test-role in 1"));
		EasyMock.replay(loggerSink, roleDao);
		
		UIRole role = new UIRole();
		role.setRoleName("test-role");
		role.setSubsystemId(1L);
		roleService.createRole(role);
		
		EasyMock.verify(loggerSink);
	}

    @Test
	public void testUpdateRole() throws Exception {
		roleDao.updateRole(anyObject(UIRole.class));
		EasyMock.expectLastCall().andReturn(okResult());
		loggerSink.info(anyObject(Logger.class), eq("UPDATE_ROLE"), eq(true), eq("test-role in 1"));
		EasyMock.replay(loggerSink, roleDao);
		
		UIRole role = new UIRole();
		role.setRoleName("test-role");
		role.setSubsystemId(1L);
		roleService.updateRole(role);
		
		EasyMock.verify(loggerSink);
	}

    @Test
	public void testUpdateRoleFail() throws Exception {
		roleDao.updateRole(anyObject(UIRole.class));
		EasyMock.expectLastCall().andReturn(failureResult());
		loggerSink.info(anyObject(Logger.class), eq("UPDATE_ROLE"), eq(false), eq("test-role in 1"));
		EasyMock.replay(loggerSink, roleDao);
		
		UIRole role = new UIRole();
		role.setRoleName("test-role");
		role.setSubsystemId(1L);
		roleService.updateRole(role);
		
		EasyMock.verify(loggerSink);
	}

    @Test
	public void testDeleteRole() throws Exception {
		roleDao.deleteRole(anyLong());
		EasyMock.expectLastCall().andReturn(okResult());
		loggerSink.info(anyObject(Logger.class), eq("DELETE_ROLE"), eq(true), eq("1"));
		EasyMock.replay(loggerSink, roleDao);
		
		roleService.deleteRole(1L);
		
		EasyMock.verify(loggerSink);
	}

    @Test
	public void testDeleteRoleFail() throws Exception {
		roleDao.deleteRole(anyLong());
		EasyMock.expectLastCall().andReturn(failureResult());
		loggerSink.info(anyObject(Logger.class), eq("DELETE_ROLE"), eq(false), eq("1"));
		EasyMock.replay(loggerSink, roleDao);
		
		roleService.deleteRole(1L);
		
		EasyMock.verify(loggerSink);
	}

    @Test
	public void testChangeRoleGroups() throws Exception {
		roleDao.changeRoleGroups(anyLong(), anyObject(String.class), anyObject(String.class));
		EasyMock.expectLastCall().andReturn(okResult());
		loggerSink.info(anyObject(Logger.class), eq("CHANGE_ROLE_GROUPS"), eq(true), eq("1"));
		EasyMock.replay(loggerSink, roleDao);
		
		roleService.changeRoleGroups(1L, Collections.<Long>emptyList(), Collections.<Long>emptyList());
		
		EasyMock.verify(loggerSink);
	}

    @Test
	public void testChangeRoleGroupsFail() throws Exception {
		roleDao.changeRoleGroups(anyLong(), anyObject(String.class), anyObject(String.class));
		EasyMock.expectLastCall().andReturn(failureResult());
		loggerSink.info(anyObject(Logger.class), eq("CHANGE_ROLE_GROUPS"), eq(false), eq("1"));
		EasyMock.replay(loggerSink, roleDao);
		
		roleService.changeRoleGroups(1L, Collections.<Long>emptyList(), Collections.<Long>emptyList());
		
		EasyMock.verify(loggerSink);
	}

    @Test
	public void testChangeRoleActions() throws Exception {
		roleDao.changeRoleActions(anyLong(), anyObject(String.class), anyObject(String.class));
		EasyMock.expectLastCall().andReturn(okResult());
		loggerSink.info(anyObject(Logger.class), eq("CHANGE_ROLE_ACTIONS"), eq(true), eq("1"));
		EasyMock.replay(loggerSink, roleDao);
		
		roleService.changeRoleActions(1L, Collections.<Long>emptyList(), Collections.<Long>emptyList());
		
		EasyMock.verify(loggerSink);
	}

    @Test
	public void testChangeRoleActionsFail() throws Exception {
		roleDao.changeRoleActions(anyLong(), anyObject(String.class), anyObject(String.class));
		EasyMock.expectLastCall().andReturn(failureResult());
		loggerSink.info(anyObject(Logger.class), eq("CHANGE_ROLE_ACTIONS"), eq(false), eq("1"));
		EasyMock.replay(loggerSink, roleDao);
		
		roleService.changeRoleActions(1L, Collections.<Long>emptyList(), Collections.<Long>emptyList());
		
		EasyMock.verify(loggerSink);
	}
}
