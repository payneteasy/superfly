package com.payneteasy.superfly.web.security;

import junit.framework.TestCase;

import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.SecurityContextHolder;

import com.payneteasy.superfly.service.UserInfoService;

public class UserInfoServiceAcegiTest extends TestCase {
	
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}
	
	public void testGetUsername() {
		SecurityContextHolder.getContext().setAuthentication(new NameOnlyAuthentication());
		UserInfoService service = new UserInfoServiceAcegi();
		String username = service.getUsername();
		assertEquals("test-user", username);
	}
	
	public void testGetUsernameWhenNoAuthentication() {
		UserInfoService service = new UserInfoServiceAcegi();
		String username = service.getUsername();
		assertEquals(null, username);
	}
	
	private final class NameOnlyAuthentication implements Authentication {
		public GrantedAuthority[] getAuthorities() {
			return null;
		}

		public Object getCredentials() {
			return null;
		}

		public Object getDetails() {
			return null;
		}

		public Object getPrincipal() {
			return null;
		}

		public boolean isAuthenticated() {
			return true;
		}

		public void setAuthenticated(boolean isAuthenticated)
				throws IllegalArgumentException {
		}

		public String getName() {
			return "test-user";
		}
	}
}
