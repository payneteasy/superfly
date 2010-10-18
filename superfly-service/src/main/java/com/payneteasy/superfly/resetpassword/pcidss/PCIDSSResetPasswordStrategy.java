package com.payneteasy.superfly.resetpassword.pcidss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.service.LoggerSink;

public class PCIDSSResetPasswordStrategy implements ResetPasswordStrategy {
	private static final Logger logger = LoggerFactory.getLogger(PCIDSSResetPasswordStrategy.class);
	
	private UserDao userDao;
    private String policyName;
    private SaltSource saltSource;
    private PasswordEncoder passwordEncoder;
    private LoggerSink loggerSink;
	public PCIDSSResetPasswordStrategy(UserDao userDao,SaltSource saltSource,PasswordEncoder passwordEncoder,LoggerSink loggerSink,String policyName) {
		this.userDao = userDao;
		this.saltSource = saltSource;
		this.passwordEncoder = passwordEncoder;
		this.policyName = policyName;
		this.loggerSink = loggerSink;
	}

	public void resetPassword(long userId, String username, String password) {
           RoutineResult result= userDao.resetPassword(userId, passwordEncoder.encode(password, saltSource.getSalt(username)));
           loggerSink.info(logger, "RESET_PASSWORD", result.isOk(), String.format("%s->%s", userId, password));
	}

	public String getPolicyName() {
		return policyName;
	}

}
