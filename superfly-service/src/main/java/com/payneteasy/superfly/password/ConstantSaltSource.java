package com.payneteasy.superfly.password;

/**
 * Salt source which always returns the same constant value.
 * 
 * @author Roman Puchkovskiy
 */
public class ConstantSaltSource implements SaltSource {
	
	private String salt;
	
	public ConstantSaltSource() {
	}

	public ConstantSaltSource(String salt) {
		super();
		this.salt = salt;
	}
	
	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getSalt(String username) {
		return salt;
	}

}
