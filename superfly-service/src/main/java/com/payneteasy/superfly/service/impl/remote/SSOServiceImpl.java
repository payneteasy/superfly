package com.payneteasy.superfly.service.impl.remote;

import org.springframework.beans.factory.annotation.Required;

import com.payneteasy.superfly.api.ActionDescription;
import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.service.InternalSSOService;

/**
 * Implementation of SSOService.
 * 
 * @author Roman Puchkovskiy
 */
public class SSOServiceImpl implements SSOService {
	
	private InternalSSOService internalSSOService;
	private SubsystemIdentifierObtainer subsystemIdentifierObtainer = new AuthRequestInfoObtainer();

	@Required
	public void setInternalSSOService(InternalSSOService internalSSOService) {
		this.internalSSOService = internalSSOService;
	}

	public void setSubsystemIdentifierObtainer(
			SubsystemIdentifierObtainer subsystemIdentifierObtainer) {
		this.subsystemIdentifierObtainer = subsystemIdentifierObtainer;
	}

	/**
	 * @see SSOService#authenticate(String, String, AuthenticationRequestInfo)
	 */
	public SSOUser authenticate(String username, String password,
			AuthenticationRequestInfo authRequestInfo) {
		return internalSSOService.authenticate(username, password,
				obtainSubsystemIdentifier(authRequestInfo.getSubsystemIdentifier()),
				authRequestInfo.getIpAddress(),
				authRequestInfo.getSessionInfo());
	}

	/**
	 * @see SSOService#sendSystemData(String, com.payneteasy.superfly.api.ActionDescription[])
	 */
	public void sendSystemData(String systemIdentifier,
			ActionDescription[] actionDescriptions) {
		internalSSOService.saveSystemData(obtainSubsystemIdentifier(systemIdentifier),
				actionDescriptions);
	}
	
	protected String obtainSubsystemIdentifier(String systemIdentifier) {
		return subsystemIdentifierObtainer.obtainSubsystemIdentifier(systemIdentifier);
	}

}
