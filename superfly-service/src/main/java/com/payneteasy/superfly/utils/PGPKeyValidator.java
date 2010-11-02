package com.payneteasy.superfly.utils;

import com.payneteasy.superfly.api.BadPublicKeyException;
import com.payneteasy.superfly.crypto.PublicKeyCrypto;

/**
 * Implements PGP keys validation logic.
 *
 * @author Roman Puchkovskiy
 */
public class PGPKeyValidator {
	/**
	 * Validates a PGP public key.
	 * 
	 * @param value		value to validate
	 * @param crypto	crypto implementation to carry out additional checks
	 * @throws BadPublicKeyException thrown if validation fails; getMessage()
	 * contains error code
	 */
	public static void validatePublicKey(String value, PublicKeyCrypto crypto) throws BadPublicKeyException {
		if (value != null) {
			int open = value.indexOf("-----BEGIN PGP PUBLIC KEY BLOCK-----");
			int close = value.indexOf("-----END PGP PUBLIC KEY BLOCK-----");
			if (open < 0) {
				throw new BadPublicKeyException("PublicKeyValidator.noBeginBlock");
			}
			if (close < 0) {
				throw new BadPublicKeyException("PublicKeyValidator.noEndBlock");
			}
			if (open >= 0 && close >= 0 &&open >= close) {
				throw new BadPublicKeyException("PublicKeyValidator.wrongBlockOrder");
			}
			
			if (!crypto.isPublicKeyValid(value)) {
				throw new BadPublicKeyException("PublicKeyValidator.invalidKey");
			}
		}
	}
}
