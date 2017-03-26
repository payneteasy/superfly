package com.payneteasy.superfly.lockout.none;

import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.model.LockoutType;

public class NoneLockoutStrategy implements LockoutStrategy {

    public void checkLoginsFailed(String userName, LockoutType lockoutType) {

    }

}
