package com.payneteasy.superfly.security.validator;

import java.util.HashSet;
import java.util.Set;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.exception.PreconditionsException;

/**
 * Validator which validates that {@link CompoundAuthentication} contains
 * all the required authentications.
 *
 * @author Roman Puchkovskiy
 */
@Setter
@Accessors(chain = true)
public class CompoundAuthenticationValidator implements AuthenticationValidator {

    private Class<?>[] requiredClasses = new Class<?>[]{};

    public void validate(Authentication auth) throws AuthenticationException {
        if (auth instanceof CompoundAuthentication compound) {
            validateCompound(compound);
        } else {
            throw new IllegalArgumentException(auth.getClass().getName() + " is not instance of a CompoundAuthentication");
        }
    }

    protected void validateCompound(CompoundAuthentication compound) {
        Authentication[] auths = compound.getReadyAuthentications();
        Set<Class<?>> classes = new HashSet<Class<?>>();
        for (Authentication auth : auths) {
            classes.add(auth.getClass());
        }
        for (Class<?> clazz : requiredClasses) {
            if (!classes.contains(clazz)) {
                throw new PreconditionsException("Unexpected authentication class before authentication");
            }
        }
    }

}
