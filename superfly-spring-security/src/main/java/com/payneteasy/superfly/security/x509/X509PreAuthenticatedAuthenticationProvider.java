package com.payneteasy.superfly.security.x509;

import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

public class X509PreAuthenticatedAuthenticationProvider extends PreAuthenticatedAuthenticationProvider {
  public X509PreAuthenticatedAuthenticationProvider(UserDetailsService userDetailsService) {
    setPreAuthenticatedUserDetailsService(new UserDetailsByNameServiceWrapper<>(userDetailsService));
  }
}
