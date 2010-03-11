package com.payneteasy.superfly.common.password;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Password handler that uses SHA-1 hash function to encrypt a password.
 * 
 * @author Roman Puchkovskiy
 */
public class ShaPasswordHandler extends AbstractSymmetricPasswordHandler {

	@Override
	protected String doEncryptPassword(String password) {
		return DigestUtils.shaHex(password);
	}

}
