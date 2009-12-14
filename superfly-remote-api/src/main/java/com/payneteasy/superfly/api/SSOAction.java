package com.payneteasy.superfly.api;

/**
 * SSO action (corresponds to a role in JEE security model).
 * 
 * @author Roman Puchkovskiy
 */
public class SSOAction {
	private String name;
	private boolean loggingNeeded;
	
	public SSOAction(String name, boolean loggingNeeded) {
		super();
		this.name = name;
		this.loggingNeeded = loggingNeeded;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isLoggingNeeded() {
		return loggingNeeded;
	}

	public void setLoggingNeeded(boolean loggingNeeded) {
		this.loggingNeeded = loggingNeeded;
	}

}
