package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.service.UserInfoService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;

public class UserInfoServiceAcegiTest {

    @After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

    @Test
	public void testGetUsername() {
		SecurityContextHolder.getContext().setAuthentication(new NameOnlyAuthentication());
		UserInfoService service = new UserInfoServiceAcegi();
		String username = service.getUsername();
		Assert.assertEquals("test-user", username);
	}

    @Test
	public void testGetUsernameWhenNoAuthentication() {
		UserInfoService service = new UserInfoServiceAcegi();
		String username = service.getUsername();
        Assert.assertEquals(null, username);
	}
	
	private final class NameOnlyAuthentication implements Authentication {
		public Collection<GrantedAuthority> getAuthorities() {
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
