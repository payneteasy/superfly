package com.payneteasy.superfly.lockout;

public interface LockoutStrategy {
   void checkLoginsFailed(String userName, String password);
}
