package com.payneteasy.superfly.resetpassword.pcidss;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;

public class PCIDSSResetPasswordStrategy implements ResetPasswordStrategy {
	private UserDao userDao;
    private String policyName;
    private SaltSource saltSource;
    private PasswordEncoder passwordEncoder;
	public PCIDSSResetPasswordStrategy(UserDao userDao,SaltSource saltSource,PasswordEncoder passwordEncoder,String policyName) {
		this.userDao = userDao;
		this.saltSource = saltSource;
		this.passwordEncoder = passwordEncoder;
		this.policyName = policyName;
	}

	public void resetPassword(long userId, String username, String password) {
           userDao.resetPassword(userId, passwordEncoder.encode(password, saltSource.getSalt(username)));
	}

	public String getPolicyName() {
		return policyName;
	}

}
