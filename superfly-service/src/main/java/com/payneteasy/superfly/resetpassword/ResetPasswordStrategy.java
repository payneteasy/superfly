package com.payneteasy.superfly.resetpassword;

public interface ResetPasswordStrategy {
	void resetPassword(long userId, String userName, String password);

	String getPolicyName();
}
