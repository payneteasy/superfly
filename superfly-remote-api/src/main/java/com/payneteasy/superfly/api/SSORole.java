package com.payneteasy.superfly.api;

import java.io.Serializable;

/**
 * Role: defines possible actions, plus it specifies database 'profile'.
 * It's not a JEE role equivalent.
 * 
 * @author Roman Puchkovskiy
 */
public class SSORole implements Serializable {
	private String name;

	public SSORole(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SSORole other = (SSORole) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
