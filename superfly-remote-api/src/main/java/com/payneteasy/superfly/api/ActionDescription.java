package com.payneteasy.superfly.api;

import java.io.Serializable;

/**
 * Describes an action. Used to send info about an action from subsystem to
 * Superfly server.
 * 
 * @author Roman Puchkovskiy
 */
public class ActionDescription implements Serializable {
	private String name;
	private String description;
	
	public ActionDescription() {
	}
	
	public ActionDescription(String name) {
		super();
		this.name = name;
	}

	public ActionDescription(String name, String description) {
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
