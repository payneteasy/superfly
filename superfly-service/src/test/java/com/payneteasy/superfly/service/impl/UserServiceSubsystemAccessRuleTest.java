package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.UserDao;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * A subsystem signs in and reads users with a role in it; it changes only those of them that have no role
 * in the local (admin UI) subsystem.
 */
public class UserServiceSubsystemAccessRuleTest {

    private static final String LOCAL  = "superfly";
    private static final String CALLER = "billing";
    private static final String USER   = "user";

    private UserDao userDao;
    private UserServiceImpl service;

    @Before
    public void setUp() {
        userDao = createMock(UserDao.class);
        service = new UserServiceImpl();
        service.setUserDao(userDao);
    }

    private void givenRoles(boolean inLocal, boolean inCaller) {
        expect(userDao.userHasRolesInSubsystem(USER, LOCAL)).andStubReturn(inLocal ? "Y" : "N");
        expect(userDao.userHasRolesInSubsystem(USER, CALLER)).andStubReturn(inCaller ? "Y" : "N");
        replay(userDao);
    }

    @Test
    public void userOfTheCallerIsAccessibleAndManageable() {
        givenRoles(false, true);

        assertTrue(service.isUserAccessibleFrom(USER, CALLER));
        assertTrue(service.isUserManageableFrom(USER, CALLER));
    }

    @Test
    public void superflyAdminWithRoleInTheCallerIsAccessibleButNotManageable() {
        givenRoles(true, true);

        assertTrue(service.isUserAccessibleFrom(USER, CALLER));
        assertFalse(service.isUserManageableFrom(USER, CALLER));
    }

    @Test
    public void superflyAdminWithoutRoleInTheCallerIsNeitherAccessibleNorManageable() {
        givenRoles(true, false);

        assertFalse(service.isUserAccessibleFrom(USER, CALLER));
        assertFalse(service.isUserManageableFrom(USER, CALLER));
    }

    @Test
    public void userWithoutRolesIsNeitherAccessibleNorManageable() {
        // a user of another subsystem and an unknown user get the same answers here
        givenRoles(false, false);

        assertFalse(service.isUserAccessibleFrom(USER, CALLER));
        assertFalse(service.isUserManageableFrom(USER, CALLER));
    }

    @Test
    public void nullArgumentsAreNeitherAccessibleNorManageable() {
        replay(userDao);

        assertFalse(service.isUserAccessibleFrom(null, CALLER));
        assertFalse(service.isUserAccessibleFrom(USER, null));
        assertFalse(service.isUserManageableFrom(null, CALLER));
        assertFalse(service.isUserManageableFrom(USER, null));
        verify(userDao);
    }
}
