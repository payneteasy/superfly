package com.payneteasy.superfly.security_2_0.authentication;

import org.springframework.security.Authentication;

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
