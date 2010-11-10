package com.payneteasy.superfly.resetpassword.pcidss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.password.UserPasswordEncoder;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.LoggerSink;

public class PCIDSSResetPasswordStrategy implements ResetPasswordStrategy {
	private static final Logger logger = LoggerFactory.getLogger(PCIDSSResetPasswordStrategy.class);
	
	private UserDao userDao;
	private UserPasswordEncoder userPasswordEncoder;
    private String policyName;
    private LoggerSink loggerSink;
    
	public PCIDSSResetPasswordStrategy(UserDao userDao, UserPasswordEncoder userPasswordEncoder, LoggerSink loggerSink, String policyName) {
		this.userDao = userDao;
		this.userPasswordEncoder = userPasswordEncoder;
		this.policyName = policyName;
		this.loggerSink = loggerSink;
	}

	public void resetPassword(long userId, String username, String password) {
        RoutineResult result = userDao.resetPassword(userId, password==null ? password : userPasswordEncoder.encode(password, username));
        loggerSink.info(logger, "RESET_PASSWORD", result.isOk(), String.format("%s", username));
	}

	public String getPolicyName() {
		return policyName;
	}

}
