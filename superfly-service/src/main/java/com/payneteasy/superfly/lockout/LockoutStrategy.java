package com.payneteasy.superfly.lockout;

import com.payneteasy.superfly.model.LockoutType;

public interface LockoutStrategy {
   void checkLoginsFailed(String userName, LockoutType lockoutType);
}
