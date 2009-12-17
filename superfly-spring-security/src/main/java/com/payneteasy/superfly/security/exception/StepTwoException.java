package com.payneteasy.superfly.security.exception;

import org.springframework.security.AuthenticationException;

import com.payneteasy.superfly.security.TwoStepAuthenticationProcessingFilter;

/**
 * Thrown to signal that step 2 of two-step authentication process
 * must be initiated.
 * 
 * @author Roman Puchkovskiy
 * @see TwoStepAuthenticationProcessingFilter
 */
public class StepTwoException extends AuthenticationException {

	public StepTwoException(String msg) {
		super(msg);
	}

	public StepTwoException(String msg, Throwable t) {
		super(msg, t);
	}

	public StepTwoException(String msg, Object extraInformation) {
		super(msg, extraInformation);
	}

}
