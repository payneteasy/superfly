package com.payneteasy.superfly.common.password;

/**
 * Base for {@link PasswordHandler} implementation for which password
 * encryption is symmetric; that is, if encryption function is f, then
 * f(f(p))=p.
 * You just need to provide that encryption function implementation when
 * extending this class.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class AbstractSymmetricPasswordHandler implements PasswordHandler {

	/**
	 * @see PasswordHandler#encryptPassword(String)
	 */
	public String encryptPassword(String password) {
		return doEncryptPassword(password);
	}

	/**
	 * @see PasswordHandler#checkPassword(String, String)
	 */
	public boolean checkPassword(String passwordToCheck, String encryptedPassword) {
		return doEncryptPassword(passwordToCheck).equals(encryptedPassword);
	}

	/**
	 * Encrypts a password. Transformation must be symmetric.
	 * 
	 * @param password	password to encrypt
	 * @return transformed password
	 */
	protected abstract String doEncryptPassword(String password);

}
