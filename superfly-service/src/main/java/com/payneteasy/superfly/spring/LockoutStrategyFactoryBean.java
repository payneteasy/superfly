package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.lockout.pcidss.PCIDSSLockoutStrategy;
import com.payneteasy.superfly.service.UserService;

public class LockoutStrategyFactoryBean extends AbstractPolicyDependingFactoryBean<LockoutStrategy> {
    private LockoutStrategy lockoutStrategy;
    private Long maxLoginsFailed;
    private UserService userService;

    public void setMaxLoginsFailed(Long maxLoginsFailed) {
        this.maxLoginsFailed = maxLoginsFailed;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public LockoutStrategy getObject() throws Exception {
        if (lockoutStrategy == null) {
            Policy p = findPolicyByIdentifier();
            switch (p) {
            case NONE:
                lockoutStrategy = new NoneLockoutStrategy();
                break;
            case PCIDSS:
                lockoutStrategy = new PCIDSSLockoutStrategy(userService, maxLoginsFailed);
                break;
            default:
                throw new IllegalArgumentException();
            }
        }
        return lockoutStrategy;
    }

    public Class<?> getObjectType() {
        return LockoutStrategy.class;
    }

    public boolean isSingleton() {
        return true;
    }

}
