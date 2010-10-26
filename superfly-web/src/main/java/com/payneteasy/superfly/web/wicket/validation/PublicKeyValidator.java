package com.payneteasy.superfly.web.wicket.validation;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class PublicKeyValidator implements IValidator<String> {

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
			if (open >= close) {
				triggerError(validatable, "PublicKeyValidator.wrongBlockOrder");
			}
		}
	}

	private void triggerError(IValidatable<String> validatable, String key) {
		ValidationError error = new ValidationError();
		error.addMessageKey(key);
		validatable.error(error);
	}

}
