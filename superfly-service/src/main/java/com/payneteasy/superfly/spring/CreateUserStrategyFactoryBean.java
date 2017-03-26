package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.policy.create.CreateUserStrategy;
import com.payneteasy.superfly.policy.create.none.NoneCreateUserStrategy;
import com.payneteasy.superfly.policy.create.pcidss.PCIDSSCreateUserStrategy;
import com.payneteasy.superfly.register.RegisterUserStrategy;
import com.payneteasy.superfly.register.none.NoneRegisterUserStrategy;
import com.payneteasy.superfly.register.pcidss.PCIDSSRegisterUserStrategy;

public class CreateUserStrategyFactoryBean extends AbstractPolicyDependingFactoryBean<CreateUserStrategy> {
    private CreateUserStrategy createUserStrategy;
    private UserDao userDao;

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public CreateUserStrategy getObject() throws Exception {
        if (createUserStrategy == null) {
            Policy p = findPolicyByIdentifier();
            switch (p) {
            case NONE:
                createUserStrategy = new NoneCreateUserStrategy(userDao);
                break;
            case PCIDSS:
                createUserStrategy = new PCIDSSCreateUserStrategy(userDao);
                break;
            default:
                throw new IllegalArgumentException();
            }
        }
        return createUserStrategy;
    }

    public Class<?> getObjectType() {
        return CreateUserStrategy.class;
    }

    public boolean isSingleton() {
        return true;
    }

}
