package com.payneteasy.superfly.spring;

import org.springframework.beans.factory.FactoryBean;

import com.payneteasy.superfly.password.MessageDigestPasswordEncoder;
import com.payneteasy.superfly.password.PasswordEncoder;

/**
 * {@link FactoryBean} for {@link PasswordEncoder}. Produces a value in
 * according to the given policy.
 * 
 * @author Roman Puchkovskiy
 */
public class PasswordEncoderFactoryBean extends AbstractPolicyDependingFactoryBean<PasswordEncoder> {

    private PasswordEncoder encoder;

    public PasswordEncoder getObject() throws Exception {
        if (encoder == null) {
            Policy p = findPolicyByIdentifier();
            switch (p) {
            case NONE:
            case PCIDSS:
                MessageDigestPasswordEncoder encPCIDSS = new MessageDigestPasswordEncoder();
                encPCIDSS.setAlgorithm("SHA-256");
                encoder = encPCIDSS;
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
