package com.payneteasy.superfly.lockout.pcidss;

import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.LockoutType;
import com.payneteasy.superfly.service.UserService;

public class PCIDSSLockoutStrategy implements LockoutStrategy {

    private final UserService userService;
    private Long maxLoginsFailed;
    
    public PCIDSSLockoutStrategy(UserService userService, Long maxLoginsFailed){
        this.userService=userService;
        this.maxLoginsFailed=maxLoginsFailed;
    }


    public void checkLoginsFailed(String userName, LockoutType lockoutType) {
        userService.lockoutConditionnally(userName, maxLoginsFailed, lockoutType.name());
    }
}
