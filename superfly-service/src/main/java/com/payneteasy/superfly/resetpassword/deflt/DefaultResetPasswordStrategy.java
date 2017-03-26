package com.payneteasy.superfly.resetpassword.deflt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.password.UserPasswordEncoder;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.LoggerSink;

public class DefaultResetPasswordStrategy implements ResetPasswordStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DefaultResetPasswordStrategy.class);

    private UserDao userDao;
    private UserPasswordEncoder userPasswordEncoder;
    private LoggerSink loggerSink;
    
    public DefaultResetPasswordStrategy(UserDao userDao, UserPasswordEncoder userPasswordEncoder, LoggerSink loggerSink) {
        this.userDao = userDao;
        this.userPasswordEncoder = userPasswordEncoder;
        this.loggerSink = loggerSink;
    }

    public void resetPassword(long userId, String username, String password) {
        RoutineResult result = userDao.resetPassword(userId, password==null ? null : userPasswordEncoder.encode(password, username));
        loggerSink.info(logger, "RESET_PASSWORD", result.isOk(), String.format("%s", username));
    }

}
