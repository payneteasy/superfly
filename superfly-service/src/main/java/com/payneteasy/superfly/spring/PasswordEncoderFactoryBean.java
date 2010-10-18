package com.payneteasy.superfly.spring;

import org.springframework.beans.factory.FactoryBean;

import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.PasswordEncoder;
import com.payneteasy.superfly.password.PlaintextPasswordEncoder;

/**
 * {@link FactoryBean} for {@link PasswordEncoder}. Produces a value in
 * according to the given policy.
 * 
 * @author Roman Puchkovskiy
 */
public class PasswordEncoderFactoryBean extends AbstractPolicyDependingFactoryBean {
	
	private PasswordEncoder encoder;

	public Object getObject() throws Exception {
		if (encoder == null) {
			Policy p = findPolicyByIdentifier();
			switch (p) {
			case NONE:
				encoder = new PlaintextPasswordEncoder();
				break;
			case PCIDSS:
				MessageDigestPasswordEncoder enc = new MessageDigestPasswordEncoder();
				enc.setAlgorithm("SHA-256");
				encoder = enc;
				break;
			default:
				throw new IllegalArgumentException();
			}
		}
		return encoder;
	}

	public Class<?> getObjectType() {
		return PasswordEncoder.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
