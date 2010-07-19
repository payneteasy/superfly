package com.payneteasy.superfly.api;

import java.io.Serializable;

/**
 * Specification for role grant. Includes subsystem specification and
 * role specification itself.
 * Subsystem is specified either by explicit subsystem identifier (if
 * detectSubsystemIdentifier is false) or it's detected by a detector.
 * Role is specified by principal name.
 * 
 * @author Roman Puchkovskiy
 * @since 1.1
 */
public class RoleGrantSpecification implements Serializable {
	private static final long serialVersionUID = 5027521935601493108L;
	
	private String subsystemIdentifier;
	private boolean detectSubsystemIdentifier;
	private String principalName;

	public String getSubsystemIdentifier() {
		return subsystemIdentifier;
	}

	public void setSubsystemIdentifier(String subsystemIdentifier) {
		this.subsystemIdentifier = subsystemIdentifier;
	}

	public boolean isDetectSubsystemIdentifier() {
		return detectSubsystemIdentifier;
	}

	public void setDetectSubsystemIdentifier(boolean detectSubsystemIdentifier) {
		this.detectSubsystemIdentifier = detectSubsystemIdentifier;
	}

	public String getPrincipalName() {
		return principalName;
	}

	public void setPrincipalName(String principalName) {
		this.principalName = principalName;
	}
}
