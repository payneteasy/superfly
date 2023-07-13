package com.payneteasy.superfly.web.security.exception;


import org.springframework.security.core.AuthenticationException;

public class SubsystemNotFoundException extends AuthenticationException {
  public SubsystemNotFoundException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public SubsystemNotFoundException(String msg) {
    super(msg);
  }
}
