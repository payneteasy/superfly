package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.register.RegisterUserStrategy;
import com.payneteasy.superfly.register.none.NoneRegisterUserStrategy;
import com.payneteasy.superfly.register.pcidss.PCIDSSRegisterUserStrategy;
import com.payneteasy.superfly.service.UserService;

public class RegisterUserStrategyFactoryBean extends AbstractPolicyDependingFactoryBean<RegisterUserStrategy> {
    private RegisterUserStrategy registerUserStrategy;
    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public RegisterUserStrategy getObject() throws Exception {
        if (registerUserStrategy == null) {
            Policy p = findPolicyByIdentifier();
            switch (p) {
            case NONE:
                registerUserStrategy = new NoneRegisterUserStrategy(userService);
                break;
            case PCIDSS:
                registerUserStrategy = new PCIDSSRegisterUserStrategy(userService);
                break;
            default:
                throw new IllegalArgumentException();
            }
        }
        return registerUserStrategy;
    }

    public Class<?> getObjectType() {
        return RegisterUserStrategy.class;
    }

    public boolean isSingleton() {
        return true;
    }

}
