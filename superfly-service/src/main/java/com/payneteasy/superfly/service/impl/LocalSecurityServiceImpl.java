package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.model.LockoutType;
import com.payneteasy.superfly.password.UserPasswordEncoder;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.spi.HOTPProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Transactional
public class LocalSecurityServiceImpl implements LocalSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(LocalSecurityServiceImpl.class);

    private UserDao userDao;
    private String localSubsystemName = "superfly";
    private String localRoleName = "admin";
    private LoggerSink loggerSink;
    private UserPasswordEncoder userPasswordEncoder;
    private HOTPProvider hotpProvider;
    private LockoutStrategy lockoutStrategy;

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setLocalSubsystemName(String localSubsystemName) {
        this.localSubsystemName = localSubsystemName;
    }

    public void setLocalRoleName(String localRoleName) {
        this.localRoleName = localRoleName;
    }

    @Required
    public void setLoggerSink(LoggerSink loggerSink) {
        this.loggerSink = loggerSink;
    }

    @Required
    public void setUserPasswordEncoder(UserPasswordEncoder userPasswordEncoder) {
        this.userPasswordEncoder = userPasswordEncoder;
    }

    @Required
    public void setHotpProvider(HOTPProvider hotpProvider) {
        this.hotpProvider = hotpProvider;
    }

    @Required
    public void setLockoutStrategy(LockoutStrategy lockoutStrategy) {
        this.lockoutStrategy = lockoutStrategy;
    }

    public String[] authenticate(String username, String password) {
        String encPassword = userPasswordEncoder.encode(password, username);
        AuthSession session = userDao.authenticate(username, encPassword,
                localSubsystemName, null, null);
        AuthRole role = null;
        if (session != null) {
            if (session.getRoles().size() == 1 && session.getRoles().get(0).getRoleName() == null) {
                // actually, it's empty
                session.setRoles(Collections.<AuthRole>emptyList());
            }
            for (AuthRole r : session.getRoles()) {
                if (localRoleName.equals(r.getRoleName())) {
                    role = r;
                    break;
                }
            }
        }
        if (role != null) {
            String[] result = new String[role.getActions().size()];
            for (int i = 0; i < result.length; i++) {
                result[i] = role.getActions().get(i).getActionName();
            }
            loggerSink.info(logger, "LOCAL_LOGIN", true, username);
            return result;
        }
        if (session == null || session.getRoles().isEmpty()) {
            lockoutStrategy.checkLoginsFailed(username, LockoutType.PASSWORD);
        }
        loggerSink.info(logger, "LOCAL_LOGIN", false, username);
        return null;
    }

    public boolean authenticateUsingHOTP(String username, String hotp) {
        boolean ok = hotpProvider.authenticate(localSubsystemName, username, hotp);
        if (!ok) {
            userDao.incrementHOTPLoginsFailed(username);
            lockoutStrategy.checkLoginsFailed(username, LockoutType.HOTP);
        } else {
            userDao.clearHOTPLoginsFailed(username);
        }
        loggerSink.info(logger, "LOCAL_HOTP_CHECK", false, username);
        return ok;
    }

}
