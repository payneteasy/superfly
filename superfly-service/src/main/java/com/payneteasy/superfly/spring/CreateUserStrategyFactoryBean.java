package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.policy.create.CreateUserStrategy;
import com.payneteasy.superfly.policy.create.none.NoneCreateUserStrategy;
import com.payneteasy.superfly.policy.create.pcidss.PCIDSSCreateUserStrategy;
import com.payneteasy.superfly.service.UserService;

public class CreateUserStrategyFactoryBean extends AbstractPolicyDependingFactoryBean<CreateUserStrategy> {
    private CreateUserStrategy createUserStrategy;
    private UserService userService;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public CreateUserStrategy getObject() throws Exception {
        if (createUserStrategy == null) {
            Policy p = findPolicyByIdentifier();
            switch (p) {
            case NONE:
                createUserStrategy = new NoneCreateUserStrategy(userService);
                break;
            case PCIDSS:
                createUserStrategy = new PCIDSSCreateUserStrategy(userService);
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
