package com.payneteasy.superfly.security.x509;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class X509EFailureHandler implements AuthenticationFailureHandler {
  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception
  ) throws IOException {
    request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    writeTextMessage(response, exception);

  }

  private void writeTextMessage(HttpServletResponse response, AuthenticationException exception) throws IOException {
    response.setHeader("Content-Type", "text/plain");

    PrintWriter writer = response.getWriter();
    writer.print(exception.getMessage());
    writer.close();
  }
}
