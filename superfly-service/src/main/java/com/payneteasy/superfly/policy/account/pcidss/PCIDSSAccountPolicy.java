package com.payneteasy.superfly.policy.account.pcidss;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.User;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.PasswordGenerator;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.policy.account.AccountPolicy;
import com.payneteasy.superfly.service.UserService;

/**
 * {@link AccountPolicy} which conforms to PCI-DSS requirements.
 *
 * @author Roman Puchkovskiy
 */
public class PCIDSSAccountPolicy implements AccountPolicy {
	
	private static final Logger logger = LoggerFactory.getLogger(PCIDSSAccountPolicy.class);
	
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

	public void suspendUsersIfNeeded(int days, UserService userService) {
		List<User> users = userDao.getUsersToSuspend(days);
		for (User user : users) {
			logger.debug(String.format("Suspending user [%s] with id=%d", user.getUserName(), user.getUserid()));
			userService.suspendUser(user.getUserid());
		}
	}

	public void expirePasswordsIfNeeded(int days, UserService userService) {
        List<User> users=userDao.getUsersWithExpiredPasswords(days);
        for(User u:users){
            logger.debug(String.format("Lock user [%s] with id=%d",u.getUserName(),u.getUserid()));
            userService.lockUser(u.getUserid());
        }
	}

}
