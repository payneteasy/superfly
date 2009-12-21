package com.payneteasy.superfly.web.obtainer;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.ui.preauth.x509.SubjectDnX509PrincipalExtractor;
import org.springframework.security.ui.preauth.x509.X509PreAuthenticatedProcessingFilter;
import org.springframework.security.ui.preauth.x509.X509PrincipalExtractor;

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

	public String obtainSubsystemIdentifier(String hint) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return (String) authentication.getPrincipal();
	}

}
