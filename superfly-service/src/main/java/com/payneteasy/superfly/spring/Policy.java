package com.payneteasy.superfly.spring;

/**
 * Defines current security policies.
 * 
 * @author Roman Puchkovskiy
 */
public enum Policy {
	NONE("none"), PCIDSS("pcidss");
	
	private String identifier;
	
	private Policy(String identifier) {
		this.identifier = identifier;
	}
	
	public String getIdentifier() {
		return identifier;
	}
}
