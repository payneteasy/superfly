package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import com.payneteasy.superfly.resetpassword.none.NoneResetPasswordStrategy;
import com.payneteasy.superfly.resetpassword.pcidss.PCIDSSResetPasswordStrategy;

public class ResetPasswordStrategyFactoryBean extends AbstractPolicyDependingFactoryBean {
	private ResetPasswordStrategy resetPasswordStrategy;
	private UserDao userDao;
	private SaltSource saltSource;
	private PasswordEncoder passwordEncoder;

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	public void setSaltSource(SaltSource saltSource) {
		this.saltSource = saltSource;
	}

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
				resetPasswordStrategy = new PCIDSSResetPasswordStrategy(userDao, saltSource, passwordEncoder,
						p.getIdentifier());
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
