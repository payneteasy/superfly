package com.payneteasy.superfly.security.authentication;

import org.springframework.security.core.Authentication;

/**
 * Allows to implement some operations (like determining whether we have an
 * authority) fast.
 * 
 * @author Roman Puchkovskiy
 *
 */
public interface FastAuthentication extends Authentication {
	boolean hasAuthority(String name);
}
