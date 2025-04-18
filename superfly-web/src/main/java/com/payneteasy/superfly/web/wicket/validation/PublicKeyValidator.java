package com.payneteasy.superfly.web.wicket.validation;

import com.payneteasy.superfly.api.exceptions.BadPublicKeyException;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;
import com.payneteasy.superfly.utils.PGPKeyValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

/**
 * Used to validate public keys.
 *
 * @author Roman Puchkovskiy
 */
public class PublicKeyValidator implements IValidator<String> {

    private PublicKeyCrypto crypto;

    public PublicKeyValidator(PublicKeyCrypto crypto) {
        super();
        this.crypto = crypto;
    }

    public void validate(IValidatable<String> validatable) {
        String value = validatable.getValue();
        try {
            PGPKeyValidator.validatePublicKey(value, crypto);
        } catch (BadPublicKeyException e) {
            triggerError(validatable, e.getMessage());
        }
    }

    private void triggerError(IValidatable<String> validatable, String key) {
        ValidationError error = new ValidationError();
        error.addKey(key);
        validatable.error(error);
    }

}
