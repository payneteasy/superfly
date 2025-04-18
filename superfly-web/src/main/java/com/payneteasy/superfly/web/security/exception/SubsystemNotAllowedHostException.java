package com.payneteasy.superfly.web.security.exception;


import org.springframework.security.core.AuthenticationException;

public class SubsystemNotAllowedHostException extends AuthenticationException {
  public SubsystemNotAllowedHostException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public SubsystemNotAllowedHostException(String msg) {
    super(msg);
  }
}
