package com.payneteasy.superfly.api;

import java.io.Serializable;

/**
 * Role: defines possible actions, plus it specifies database 'profile'.
 * It's not a JEE role equivalent.
 * 
 * @author Roman Puchkovskiy
 * @since 1.0
 */
public class SSORole implements Serializable {
	private static final long serialVersionUID = 2624582156582074704L;
	
	private String name;

	/**
	 * Constructs role.
	 * 
	 * @param name	role name
	 */
	public SSORole(String name) {
		super();
		this.name = name;
	}

	/**
	 * Returns role name.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets role name.
	 * 
	 * @param name	name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
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
	
	@Override
	public String toString() {
		return name;
	}
}
