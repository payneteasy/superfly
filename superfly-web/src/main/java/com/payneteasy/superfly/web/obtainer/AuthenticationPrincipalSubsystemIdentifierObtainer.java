package com.payneteasy.superfly.web.obtainer;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

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
		return getUsername(authentication);
	}

	private String getUsername(Authentication authentication) {
		Object principal = authentication.getPrincipal();
		if (principal instanceof String) {
			return (String) principal;
		} else if (principal instanceof UserDetails) {
			return ((UserDetails) principal).getUsername();
		} else {
			return principal.toString();
		}
	}

}
