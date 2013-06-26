package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

/**
 * @author rpuch
 */
public class ComplexDaoTest extends AbstractDaoTest {
    private SessionDao sessionDao;
    private SubsystemDao subsystemDao;
    private UserDao userDao;
    private RoleDao roleDao;
    private ActionDao actionDao;

    private static boolean created = false;
    private static UISubsystem subsystem;
    private static UIRole role;
    private static UIUserForCreate user;

    @Autowired
    public void setSessionDao(SessionDao sessionDao) {
        this.sessionDao = sessionDao;
    }

    @Autowired
    public void setSubsystemDao(SubsystemDao subsystemDao) {
        this.subsystemDao = subsystemDao;
    }

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setRoleDao(RoleDao roleDao) {
        this.roleDao = roleDao;
    }

    @Autowired
    public void setActionDao(ActionDao actionDao) {
        this.actionDao = actionDao;
    }

    @Before
    public void setUp() {
        if (!created) {
            subsystem = new UISubsystem();
            subsystem.setName("subsystem-for-complex");
            subsystem.setCallbackUrl("no-callback");
            subsystem.setTitle("The Subsystem");
            subsystem.setSubsystemUrl("subsystem-for-complex-url");
            subsystem.setLandingUrl("subsystem-for-complex-url");
            subsystemDao.createSubsystem(subsystem);

            role = new UIRole();
            role.setRoleName("role1");
            role.setPrincipalName("role1");
            role.setSubsystemId(subsystem.getId());
            roleDao.createRole(role);

            actionDao.saveActions(subsystem.getName(),
                    Collections.singletonList(new ActionToSave("action1")));
            roleDao.changeRoleActions(role.getRoleId(), "1,2,3,4,5,6,7,8,9,10", "");

            user = new UIUserForCreate();
            user.setUsername("user-for-complex");
            user.setPassword("abc");
            user.setEmail("email-for-complex");
            user.setName("user-1");
            user.setSurname("user-1");
            user.setSecretQuestion("");
            user.setSecretAnswer("");
            user.setHotpSalt("DEADBEEF");
            userDao.createUser(user);

            created = true;
        }
    }

    @Test
    public void test() {
        SSOSession ssoSession = sessionDao.createSSOSession(user.getUsername(), "abcdef");
        Assert.assertTrue(ssoSession.getId() > 0);
        Assert.assertEquals("abcdef", ssoSession.getIdentifier());

        Assert.assertNull(sessionDao.getValidSSOSession("no-such-session"));
        SSOSession ssoSession2 = sessionDao.getValidSSOSession(ssoSession.getIdentifier());
        Assert.assertEquals(ssoSession.getId(), ssoSession2.getId());
        Assert.assertEquals("abcdef", ssoSession2.getIdentifier());

        SubsystemTokenData tokenData;
        tokenData = subsystemDao.issueSubsystemTokenIfCanLogin(999999999L, subsystem.getName(), "beef");
        Assert.assertNull("No SSO session exists", tokenData);
        tokenData = subsystemDao.issueSubsystemTokenIfCanLogin(ssoSession.getId(), "no-such-subsystem", "beef");
        Assert.assertNull("No subsystem exists", tokenData);

        tokenData = subsystemDao.issueSubsystemTokenIfCanLogin(ssoSession.getId(), subsystem.getName(), "beef");
        // cannot login there yet as no role exists in that subsystem for that user
        Assert.assertNull("User should not be able to login", tokenData);

        userDao.addSubsystemWithRole(user.getId(), role.getRoleId());
        userDao.changeUserRoleActions(user.getId(), "1,2,3,4,5,6,7,8,9,10", "");
        tokenData = subsystemDao.issueSubsystemTokenIfCanLogin(ssoSession.getId(), subsystem.getName(), "beef");
        Assert.assertNotNull("User should be able to login", tokenData);
        Assert.assertEquals("beef", tokenData.getSubsystemToken());
        Assert.assertEquals("subsystem-for-complex-url", tokenData.getLandingUrl());

        AuthSession session;
        session = userDao.exchangeSubsystemToken("no-such-token");
        Assert.assertNull(session);
        session = userDao.exchangeSubsystemToken("beef");
        Assert.assertNotNull(session);
        Assert.assertEquals("user-for-complex", session.getUsername());

        // token must be destroyed after it has been exchanged
        Assert.assertNull(userDao.exchangeSubsystemToken("beef"));

        sessionDao.touchSessions("1,2,3,4,5,6,7,8,9,10");

        sessionDao.deleteSSOSession(ssoSession.getIdentifier());
    }
}
