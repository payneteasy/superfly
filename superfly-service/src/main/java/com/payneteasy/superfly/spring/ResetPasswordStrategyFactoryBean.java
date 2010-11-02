package com.payneteasy.superfly.spring;

import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.password.UserPasswordEncoder;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.resetpassword.none.NoneResetPasswordStrategy;
import com.payneteasy.superfly.resetpassword.pcidss.PCIDSSResetPasswordStrategy;
import com.payneteasy.superfly.service.LoggerSink;

public class ResetPasswordStrategyFactoryBean extends AbstractPolicyDependingFactoryBean {
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

	public Object getObject() throws Exception {
		if (resetPasswordStrategy == null) {
			Policy p = findPolicyByIdentifier();
			switch (p) {
			case NONE:
				resetPasswordStrategy = new NoneResetPasswordStrategy(p.getIdentifier());
				break;
			case PCIDSS:
				resetPasswordStrategy = new PCIDSSResetPasswordStrategy(userDao, userPasswordEncoder,
						loggerSink, p.getIdentifier());
				break;
			default:
				throw new IllegalArgumentException();
			}
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
