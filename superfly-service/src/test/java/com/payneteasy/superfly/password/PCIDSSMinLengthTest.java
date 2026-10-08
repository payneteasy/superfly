package com.payneteasy.superfly.password;

import com.payneteasy.superfly.api.exceptions.PolicyValidationException;
import com.payneteasy.superfly.policy.password.PasswordCheckContext;
import com.payneteasy.superfly.policy.password.pcidss.PCIDSSPasswordPolicyValidation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * PCI DSS 4.0 req. 8.3.6: minimum password length is 12.
 */
public class PCIDSSMinLengthTest {

    private final PCIDSSPasswordPolicyValidation validation = new PCIDSSPasswordPolicyValidation();

    @Test
    public void elevenCharactersIsRejectedAsShort() throws Exception {
        try {
            validation.validate(new PasswordCheckContext("#asdfBsdf74"));
            fail("11 characters must be rejected");
        } catch (PolicyValidationException e) {
            assertEquals(PolicyValidationException.SHORT_PASSWORD, e.getCode());
        }
    }

    @Test
    public void twelveCharactersIsAccepted() throws Exception {
        validation.validate(new PasswordCheckContext("#asdfBsdf74x"));
    }

    @Test
    public void generatedPasswordsAreAtLeast12AndPassPolicy() throws Exception {
        PasswordGeneratorImpl generator = new PasswordGeneratorImpl();
        for (int i = 0; i < 100; i++) {
            String password = generator.generate();
            assertTrue(password.length() >= 12);
            validation.validate(new PasswordCheckContext(password));
        }
    }
}
