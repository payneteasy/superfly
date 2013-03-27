package com.payneteasy.superfly.security.validator;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.exception.PreconditionsException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;

public class CompoundAuthenticationValidatorTest {
	private CompoundAuthenticationValidator validator;

    @Before
	public void setUp() {
		validator = new CompoundAuthenticationValidator();
	}

    @Test
	public void testValidateEmptyForEmpty() {
		validator.validate(new CompoundAuthentication());
	}

    @Test
	public void testFail() {
		validator.setRequiredClasses(new Class<?>[]{UsernamePasswordAuthenticationToken.class});
		
		try {
			validator.validate(new CompoundAuthentication());
		} catch (PreconditionsException e) {
			// expected
		}
	}

    @Test
	public void testNotEmptyForNotEmpty() {
		validator.setRequiredClasses(new Class<?>[]{UsernamePasswordAuthenticationToken.class});
		CompoundAuthentication compound = new CompoundAuthentication();
		compound.addReadyAuthentication(new UsernamePasswordAuthenticationToken("user", "password", new ArrayList<GrantedAuthority>()));
		validator.validate(compound);
	}
}
