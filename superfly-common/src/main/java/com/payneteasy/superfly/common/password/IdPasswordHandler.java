package com.payneteasy.superfly.common.password;

/**
 * Password handler that does not encrypt anything.
 * 
 * @author Roman Puchkovskiy
 */
public class IdPasswordHandler extends AbstractSymmetricPasswordHandler {

	@Override
	protected String doEncryptPassword(String password) {
		return password;
	}

}
