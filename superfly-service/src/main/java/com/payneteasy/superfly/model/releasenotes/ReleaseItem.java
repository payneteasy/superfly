package com.payneteasy.superfly.model.releasenotes;

import java.io.Serializable;

/**
 * Contains info about a release item.
 */
public class ReleaseItem implements Serializable {
	private String type;
	private String name;
	private String description;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
