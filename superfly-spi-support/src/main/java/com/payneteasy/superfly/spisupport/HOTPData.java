package com.payneteasy.superfly.spisupport;

import java.io.Serializable;

import javax.persistence.Column;

/**
 * Transports HOTP data.
 *
 * @author Roman Puchkovskiy
 */
public class HOTPData implements Serializable {
	private static final long serialVersionUID = -1255491475933927511L;
	
	private String username;
	private String salt;
	private long counter;

	@Column(name = "username")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "salt")
	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	@Column(name = "counter")
	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}
}
