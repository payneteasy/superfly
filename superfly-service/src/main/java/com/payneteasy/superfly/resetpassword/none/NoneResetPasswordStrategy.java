package com.payneteasy.superfly.resetpassword.none;

import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;

public class NoneResetPasswordStrategy implements ResetPasswordStrategy {
	private String policyName;
	
	public NoneResetPasswordStrategy(String policyName) {
		this.policyName = policyName;
	}

	public void resetPassword(long userId, String username,String password) {

	}

	public String getPolicyName() {
		return policyName;
	}

}
