package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.register.RegisterUserStrategy;
import com.payneteasy.superfly.register.none.NoneRegisterUserStrategy;
import com.payneteasy.superfly.register.pcidss.PCIDSSRegisterUserStrategy;

public class RegisterUserStrategyFactoryBean extends AbstractPolicyDependingFactoryBean {
	private RegisterUserStrategy registerUserStrategy;
	private UserDao userDao;

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public Object getObject() throws Exception {
		if (registerUserStrategy == null) {
			Policy p = findPolicyByIdentifier();
			switch (p) {
			case NONE:
				registerUserStrategy = new NoneRegisterUserStrategy(userDao);
				break;
			case PCIDSS:
				registerUserStrategy = new PCIDSSRegisterUserStrategy(userDao);
				break;
			default:
				throw new IllegalArgumentException();
			}
		}
		return registerUserStrategy;
	}

	public Class getObjectType() {
		return RegisterUserStrategy.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
