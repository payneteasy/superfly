package com.payneteasy.superfly.api;

import java.io.Serializable;

/**
 * Describes a role. Used to send info about a role from subsystem to Superfly
 * server.
 * 
 * @author Roman Puchkovskiy
 */
public class RoleDescription implements Serializable {
	private String name;
	private String description;
	
	public RoleDescription() {
	}
	
	public RoleDescription(String name) {
		super();
		this.name = name;
	}

	public RoleDescription(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
