package com.payneteasy.superfly.web.obtainer;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import com.payneteasy.superfly.service.impl.remote.SubsystemIdentifierObtainer;

/**
 * Obtains subsystem identifier in the following way: returns principal
 * property from Authentication stored in the current SecurityContext.
 * This is to extract username (which is interpreted as subsystem identifier)
 * authenticated using SSL certificates, for instance.
 * 
 * @author Roman Puchkovskiy
 * @see X509PreAuthenticatedProcessingFilter
 * @see SubjectDnX509PrincipalExtractor
 * @see X509PrincipalExtractor
 */
public class AuthenticationPrincipalSubsystemIdentifierObtainer implements
		SubsystemIdentifierObtainer {

	public String obtainSubsystemIdentifier(
			AuthenticationRequestInfo authRequestInfo) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return (String) authentication.getPrincipal();
	}

}
