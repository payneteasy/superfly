package com.payneteasy.superfly.dao;

import com.payneteasy.superfly.model.ActionToSave;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.ui.role.UIRole;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.user.UIUserForCreate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Collections;

/**
 * A subsystem token is one-time, short-lived, bound to its subsystem and not issued to / exchanged for locked users.
 */
public class ExchangeSubsystemTokenDaoTest extends AbstractDaoTest {
    private SessionDao   sessionDao;
    private SubsystemDao subsystemDao;
    private UserDao      userDao;
    private RoleDao      roleDao;
    private ActionDao    actionDao;
    private JdbcTemplate jdbc;

    private static boolean          created = false;
    private static UISubsystem      subsystem;
    private static UISubsystem      foreignSubsystem;
    private static UIUserForCreate  user;
    private static SSOSession       ssoSession;

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

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Before
    public void setUp() {
        if (created) {
            return;
        }
        subsystem = createSubsystem("subsystem-for-exchange");
        foreignSubsystem = createSubsystem("foreign-subsystem-for-exchange");

        UIRole role = new UIRole();
        role.setRoleName("exchange-role");
        role.setPrincipalName("exchange-role");
        role.setSubsystemId(subsystem.getId());
        roleDao.createRole(role);
        actionDao.saveActions(subsystem.getName(), Collections.singletonList(new ActionToSave("exchange-action")));
        roleDao.changeRoleActions(role.getRoleId(), "1,2,3,4,5,6,7,8,9,10", "");

        user = new UIUserForCreate();
        user.setUsername("user-for-exchange");
        user.setPassword("abc");
        user.setEmail("email-for-exchange");
        user.setName("user-1");
        user.setSurname("user-1");
        user.setSecretQuestion("");
        user.setSecretAnswer("");
        user.setHotpSalt("DEADBEEF");
        userDao.createUser(user);
        userDao.addSubsystemWithRole(user.getId(), role.getRoleId());
        userDao.changeUserRoleActions(user.getId(), "1,2,3,4,5,6,7,8,9,10", "");

        ssoSession = sessionDao.createSSOSession(user.getUsername(), "exchange-session");
        created = true;
    }

    private UISubsystem createSubsystem(String name) {
        UISubsystem result = new UISubsystem();
        result.setName(name);
        result.setCallbackUrl("no-callback");
        result.setTitle(name);
        result.setSubsystemUrl(name + "-url");
        result.setLandingUrl(name + "-url");
        subsystemDao.createSubsystem(result);
        return result;
    }

    private void issue(String token) {
        Assert.assertNotNull(subsystemDao.issueSubsystemTokenIfCanLogin(ssoSession.getId(), subsystem.getName(), token));
    }

    private int tokenCount(String token) {
        return jdbc.queryForObject("select count(*) from subsystem_tokens where token = ?", Integer.class, token);
    }

    @Test
    public void ownFreshTokenIsExchangedOnceOnly() {
        issue("exch-once");
        Assert.assertNotNull(userDao.exchangeSubsystemToken("exch-once", subsystem.getName()));
        Assert.assertEquals(0, tokenCount("exch-once"));
        Assert.assertNull(userDao.exchangeSubsystemToken("exch-once", subsystem.getName()));
    }

    @Test
    public void foreignSubsystemCannotExchangeAndDoesNotBurnToken() {
        issue("exch-foreign");
        Assert.assertNull(userDao.exchangeSubsystemToken("exch-foreign", foreignSubsystem.getName()));
        Assert.assertEquals(1, tokenCount("exch-foreign"));
        Assert.assertNotNull(userDao.exchangeSubsystemToken("exch-foreign", subsystem.getName()));
    }

    @Test
    public void tokenOlderThanThirtySecondsIsRejected() {
        issue("exch-old");
        jdbc.update("update subsystem_tokens set created_date = now() - interval 31 second where token = ?", "exch-old");
        Assert.assertNull(userDao.exchangeSubsystemToken("exch-old", subsystem.getName()));
    }

    @Test
    public void lockedUserGetsNoTokenAndCannotExchange() {
        issue("exch-locked");
        userDao.lockUser(user.getId());
        try {
            Assert.assertNull(userDao.exchangeSubsystemToken("exch-locked", subsystem.getName()));
            Assert.assertNull(subsystemDao.issueSubsystemTokenIfCanLogin(
                    ssoSession.getId(), subsystem.getName(), "exch-locked-2"));
        } finally {
            userDao.unlockUser(user.getId());
        }
    }

    @Test
    public void deleteExpiredTokensUsesSeconds() {
        issue("exch-expire");
        jdbc.update("update subsystem_tokens set created_date = now() - interval 40 second where token = ?", "exch-expire");
        sessionDao.deleteExpiredTokens(60);
        Assert.assertEquals(1, tokenCount("exch-expire"));
        sessionDao.deleteExpiredTokens(30);
        Assert.assertEquals(0, tokenCount("exch-expire"));
    }
}
