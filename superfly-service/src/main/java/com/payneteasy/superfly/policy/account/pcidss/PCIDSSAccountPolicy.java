package com.payneteasy.superfly.policy.account.pcidss;

import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.PasswordGenerator;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.policy.account.AccountPolicy;

/**
 * {@link AccountPolicy} which conforms to PCI-DSS requirements.
 *
 * @author Roman Puchkovskiy
 */
public class PCIDSSAccountPolicy implements AccountPolicy {
	
	private UserDao userDao;
	private PasswordGenerator passwordGenerator;
	private PasswordEncoder passwordEncoder;
	private SaltSource saltSource;
	
	@Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Required
	public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
		this.passwordGenerator = passwordGenerator;
	}

	@Required
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Required
	public void setSaltSource(SaltSource saltSource) {
		this.saltSource = saltSource;
	}

	public String unlockUser(long userId, boolean unlockingSuspendedUser) {
		if (unlockingSuspendedUser) {
			String password = passwordGenerator.generate();
			String encPassword = passwordEncoder.encode(password, saltSource.getSalt(userId));
			RoutineResult result = userDao.unlockSuspendedUser(userId, encPassword);
			if (!result.isOk()) {
				throw new IllegalStateException(result.getErrorMessage());
			}
			return password;
		} else {
			RoutineResult result = userDao.unlockUser(userId);
			if (!result.isOk()) {
				throw new IllegalStateException(result.getErrorMessage());
			}
			return null;
		}
	}

}
