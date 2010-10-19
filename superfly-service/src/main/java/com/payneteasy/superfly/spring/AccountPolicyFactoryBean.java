package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.resetpassword.ResetPasswordStrategy;
import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.dao.UserDao;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.PasswordGenerator;
import com.payneteasy.superfly.password.SaltSource;
import com.payneteasy.superfly.policy.account.AccountPolicy;
import com.payneteasy.superfly.policy.account.none.SimpleAccountPolicy;
import com.payneteasy.superfly.policy.account.pcidss.PCIDSSAccountPolicy;

/**
 * Factory bean for {@link AccountPolicy}.
 *
 * @author Roman Puchkovskiy
 */
public class AccountPolicyFactoryBean extends
		AbstractPolicyDependingFactoryBean {
	
	private UserDao userDao;
	
	private AccountPolicy accountPolicy;
	private PasswordEncoder passwordEncoder;
	private SaltSource saltSource;
	private PasswordGenerator passwordGenerator;
    private ResetPasswordStrategy resetPasswordStrategy;

    @Required
    public void setResetPasswordStrategy(ResetPasswordStrategy resetPasswordStrategy) {
        this.resetPasswordStrategy = resetPasswordStrategy;
    }

    @Required
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Required
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Required
	public void setSaltSource(SaltSource saltSource) {
		this.saltSource = saltSource;
	}

	@Required
	public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
		this.passwordGenerator = passwordGenerator;
	}

	public Object getObject() throws Exception {
		if (accountPolicy == null) {
			Policy policy = findPolicyByIdentifier();
			switch (policy) {
			case NONE:
				SimpleAccountPolicy simpleAccountPolicy = new SimpleAccountPolicy();
				simpleAccountPolicy.setUserDao(userDao);
				accountPolicy = simpleAccountPolicy;
				break;
			case PCIDSS:
				PCIDSSAccountPolicy pcidssAccountPolicy = new PCIDSSAccountPolicy();
				pcidssAccountPolicy.setUserDao(userDao);
				pcidssAccountPolicy.setPasswordEncoder(passwordEncoder);
				pcidssAccountPolicy.setSaltSource(saltSource);
				pcidssAccountPolicy.setPasswordGenerator(passwordGenerator);
                pcidssAccountPolicy.setResetPasswordStrategy(resetPasswordStrategy);
				accountPolicy = pcidssAccountPolicy;
				break;
			default:
				throw new IllegalStateException("Unknown policy: " + policy);
			}
		}
		return accountPolicy;
	}

	public Class<?> getObjectType() {
		return AccountPolicy.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
