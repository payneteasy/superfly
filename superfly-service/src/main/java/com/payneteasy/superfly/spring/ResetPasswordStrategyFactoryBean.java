package com.payneteasy.superfly.spring;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.password.UserPasswordEncoder;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.resetpassword.deflt.DefaultResetPasswordStrategy;
import com.payneteasy.superfly.service.LoggerSink;

public class ResetPasswordStrategyFactoryBean implements FactoryBean<ResetPasswordStrategy> {
	private ResetPasswordStrategy resetPasswordStrategy;
	private UserDao userDao;
	private UserPasswordEncoder userPasswordEncoder;
	private LoggerSink loggerSink;

	@Required
	public void setLoggerSink(LoggerSink loggerSink) {
		this.loggerSink = loggerSink;
	}

	@Required
	public void setUserPasswordEncoder(UserPasswordEncoder userPasswordEncoder) {
		this.userPasswordEncoder = userPasswordEncoder;
	}

	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public ResetPasswordStrategy getObject() throws Exception {
		if (resetPasswordStrategy == null) {
			resetPasswordStrategy = new DefaultResetPasswordStrategy(userDao,
					userPasswordEncoder, loggerSink);
		}
		return resetPasswordStrategy;
	}

	public Class<?> getObjectType() {
		return ResetPasswordStrategy.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
