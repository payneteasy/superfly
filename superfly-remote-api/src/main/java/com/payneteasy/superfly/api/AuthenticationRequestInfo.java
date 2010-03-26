package com.payneteasy.superfly.api;

import java.io.Serializable;

/**
 * Holds additional information about authentication request.
 * 
 * @author Roman Puchkovskiy
 * @since 1.0
 */
public class AuthenticationRequestInfo implements Serializable {
	private static final long serialVersionUID = -4678568813683343136L;
	
	private String ipAddress;
	private String sessionInfo;
	private String subsystemIdentifier;

	/**
	 * Returns IP address of the client who requests authentication. This is
	 * the IP of actual client, not of the subsystem to which he tries to
	 * log in.
	 * 
	 * @return client IP address
	 */
	public String getIpAddress() {
		return ipAddress;
	}

	/**
	 * Sets client's IP address.
	 * 
	 * @param ipAddress	address to set
	 * @see #getIpAddress()
	 */
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	/**
	 * Returns some textual session info attached to this authentication
	 * request.
	 * 
	 * @return session info
	 */
	public String getSessionInfo() {
		return sessionInfo;
	}

	/**
	 * Sets session info.
	 * 
	 * @param sessionInfo
	 * @see #getSessionInfo()
	 */
	public void setSessionInfo(String sessionInfo) {
		this.sessionInfo = sessionInfo;
	}

	/**
	 * Returns an identifier of a subsystem which may be used to identify a
	 * subsystem which made call. It's not mandatory to fill this field as
	 * other means may be used to identify calling subsystem (for instance,
	 * SSL certificates).
	 * 
	 * @return subsystem identifier
	 */
	public String getSubsystemIdentifier() {
		return subsystemIdentifier;
	}

	/**
	 * Sets subsystem identifier.
	 * 
	 * @param subsystemIdentifier	identifier to set
	 */
	public void setSubsystemIdentifier(String subsystemIdentifier) {
		this.subsystemIdentifier = subsystemIdentifier;
	}
	
	@Override
	public String toString() {
		return String.format("Auth request for subsystem [%s] from IP [%s], session info [%s]",
				subsystemIdentifier, ipAddress, sessionInfo);
	}
}
