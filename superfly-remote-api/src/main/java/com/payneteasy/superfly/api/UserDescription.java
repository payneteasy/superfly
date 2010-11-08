package com.payneteasy.superfly.api;

import java.io.Serializable;

/**
 * Describes a user.
 *
 * @author Roman Puchkovskiy
 * @since 1.2-4
 */
public class UserDescription implements Serializable {
	private String username;
	private String email;
	private String firstName;
	private String lastName;
	private String publicKey;

	/**
	 * Returns a username.
	 * 
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets username.
	 * 
	 * @param username	name to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Returns user's email.
	 * 
	 * @return email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets email.
	 * 
	 * @param email	email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Returns user's first name.
	 * 
	 * @return first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Sets user's first name.
	 * 
	 * @param firstName	name to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * Returns user's last name.
	 * 
	 * @return last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Sets user's last name.
	 * 
	 * @param lastName	last name to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	/**
	 * Returns user's PGP public key.
	 * 
	 * @return public key
	 */
	public String getPublicKey() {
		return publicKey;
	}

	/**
	 * Sets PGP public key.
	 * 
	 * @param publicKey	key to set
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
}
