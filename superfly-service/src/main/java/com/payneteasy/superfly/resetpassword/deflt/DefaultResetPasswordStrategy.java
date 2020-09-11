package com.payneteasy.superfly.resetpassword.deflt;

import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.password.UserPasswordEncoder;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultResetPasswordStrategy implements ResetPasswordStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DefaultResetPasswordStrategy.class);

    private final UserService userService;
    private final UserPasswordEncoder userPasswordEncoder;
    private final LoggerSink loggerSink;
    
    public DefaultResetPasswordStrategy(UserService userService, UserPasswordEncoder userPasswordEncoder, LoggerSink loggerSink) {
        this.userService = userService;
        this.userPasswordEncoder = userPasswordEncoder;
        this.loggerSink = loggerSink;
    }

    public void resetPassword(long userId, String username, String password) {
        RoutineResult result = userService.resetPassword(userId, password==null ? null : userPasswordEncoder.encode(password, username));
        loggerSink.info(logger, "RESET_PASSWORD", result.isOk(), String.format("%s", username));
    }

}
