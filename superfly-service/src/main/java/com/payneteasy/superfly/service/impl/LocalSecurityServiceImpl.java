package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.SsoDecryptException;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.AuthRole;
import com.payneteasy.superfly.model.AuthSession;
import com.payneteasy.superfly.model.LockoutType;
import com.payneteasy.superfly.model.ui.user.OtpUserDescription;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.password.UserPasswordEncoder;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Transactional
public class LocalSecurityServiceImpl implements LocalSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(LocalSecurityServiceImpl.class);

    private UserService         userService;
    @Setter
    private String              localSubsystemName = "superfly";
    @Setter
    private String              localRoleName      = "admin";
    private LoggerSink          loggerSink;
    private UserPasswordEncoder userPasswordEncoder;
    private LockoutStrategy     lockoutStrategy;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setLoggerSink(LoggerSink loggerSink) {
        this.loggerSink = loggerSink;
    }

    @Autowired
    public void setUserPasswordEncoder(UserPasswordEncoder userPasswordEncoder) {
        this.userPasswordEncoder = userPasswordEncoder;
    }

    @Autowired
    public void setLockoutStrategy(LockoutStrategy lockoutStrategy) {
        this.lockoutStrategy = lockoutStrategy;
    }

    public String[] authenticate(String username, String password) {
        String encPassword = userPasswordEncoder.encode(password, username);
        AuthSession session = userService.authenticate(username, encPassword,
                localSubsystemName, null, null);
        AuthRole role = null;
        if (session != null) {
            if (session.getRoles().size() == 1 && session.getRoles().getFirst().getRoleName() == null) {
                // actually, it's empty
                session.setRoles(Collections.emptyList());
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

        if (session == null) {
            logger.warn("Login failed. No session for user <{}>", username);
            lockoutStrategy.checkLoginsFailed(username, LockoutType.SESSION);
        } else if (session.getRoles().isEmpty()) {
            logger.warn("Login failed. There are no roles or actions for user <{}>", username);
            lockoutStrategy.checkLoginsFailed(username, LockoutType.ROLES);
        }
        loggerSink.info(logger, "LOCAL_LOGIN", false, username);
        return null;
    }

    public boolean authenticateUsingOTP(String username, String otp) {
        return userService.authenticateUsingOTP(username, otp);
    }

    public OtpUserDescription getOtpUserForDescription(String username) {
        OtpUserDescription user               = new OtpUserDescription();
        UserForDescription userForDescription = userService.getUserForDescription(username);
        if (userForDescription.getOtpType() == OTPType.GOOGLE_AUTH) {
            user.setHasOtpMasterKey(userService.getOtpMasterKeyByUsername(username) != null
                    && !userService.getOtpMasterKeyByUsername(username).isEmpty());
        }
        return user.setUserForDescription(userForDescription);
    }

    @Override
    public void persistOtpKey(OTPType otpType, String username, String key) throws SsoDecryptException {
        userService.persistOtpKey(otpType, username, key);
    }
}
