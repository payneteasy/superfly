package com.payneteasy.superfly.web.wicket.validation;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import com.payneteasy.superfly.crypto.PublicKeyCrypto;

public class PublicKeyValidator implements IValidator<String> {
	
	private PublicKeyCrypto crypto;

	public PublicKeyValidator(PublicKeyCrypto crypto) {
		super();
		this.crypto = crypto;
	}

	public void validate(IValidatable<String> validatable) {
		String value = validatable.getValue();
		if (value != null) {
			int open = value.indexOf("-----BEGIN PGP PUBLIC KEY BLOCK-----");
			int close = value.indexOf("-----END PGP PUBLIC KEY BLOCK-----");
			if (open < 0) {
				triggerError(validatable, "PublicKeyValidator.noBeginBlock");
			}
			if (close < 0) {
				triggerError(validatable, "PublicKeyValidator.noEndBlock");
			}
			if (open >= 0 && close >= 0 &&open >= close) {
				triggerError(validatable, "PublicKeyValidator.wrongBlockOrder");
			}
			
			if (validatable.isValid() && !crypto.isPublicKeyValid(value)) {
				triggerError(validatable, "PublicKeyValidator.invalidKey");
			}
		}
	}

	private void triggerError(IValidatable<String> validatable, String key) {
		ValidationError error = new ValidationError();
		error.addMessageKey(key);
		validatable.error(error);
	}

}
