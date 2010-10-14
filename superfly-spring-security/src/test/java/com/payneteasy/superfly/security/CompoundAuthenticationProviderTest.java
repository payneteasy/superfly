package com.payneteasy.superfly.security;

import java.util.ArrayList;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;

import junit.framework.TestCase;

public class CompoundAuthenticationProviderTest extends TestCase {
	
	private CompoundAuthenticationProvider provider;
	
	public void setUp() {
		provider = new CompoundAuthenticationProvider();
	}
	
	public void testSimpleToCompoundAuthentication() {
		provider.setDelegateProvider(new AbstractTestProvider() {
			public Authentication authenticate(Authentication authentication)
					throws AuthenticationException {
				return new UsernamePasswordAuthenticationToken("username", "password", new ArrayList<GrantedAuthority>());
			}
		});
		
		Authentication authentication = provider.authenticate(new UsernamePasswordAuthenticationToken("username", "password"));
		assertTrue(authentication instanceof CompoundAuthentication);
		CompoundAuthentication compoundAuth = (CompoundAuthentication) authentication;
		assertEquals(1, compoundAuth.getReadyAuthentications().length);
	}
	
	public void testCompoundToCompoundAuthentication() {
		provider.setDelegateProvider(new AbstractTestProvider() {
			public Authentication authenticate(Authentication authentication)
					throws AuthenticationException {
				return new UsernamePasswordAuthenticationToken("username", "password", new ArrayList<GrantedAuthority>());
			}
		});
		
		CompoundAuthentication compound = new CompoundAuthentication(new UsernamePasswordAuthenticationToken("username", "password"));
		compound.addReadyAuthentication(new EmptyAuthenticationToken());
		Authentication authentication = provider.authenticate(compound);
		assertTrue(authentication instanceof CompoundAuthentication);
		CompoundAuthentication compoundAuth = (CompoundAuthentication) authentication;
		assertEquals(2, compoundAuth.getReadyAuthentications().length);
		assertTrue(compoundAuth.getFirstReadyAuthentication() instanceof EmptyAuthenticationToken);
		assertTrue(compoundAuth.getLatestReadyAuthentication() instanceof UsernamePasswordAuthenticationToken);
	}
	
	public void testFailure() {
		provider.setDelegateProvider(new AbstractTestProvider() {
			public Authentication authenticate(Authentication authentication)
					throws AuthenticationException {
				throw new BadCredentialsException("bad");
			}
		});

		try {
			provider.authenticate(new UsernamePasswordAuthenticationToken("username", "password"));
			fail();
		} catch (BadCredentialsException e) {
			assertEquals("bad", e.getMessage());
		}
	}
	
	public void testSupports() {
		assertTrue(provider.supports(CompoundAuthentication.class));
		assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
		
		provider.setSupportedSimpleAuthenticationClasses(new Class<?>[]{UsernamePasswordAuthenticationToken.class});
		assertTrue(provider.supports(UsernamePasswordAuthenticationToken.class));
	}
	
	public void testAuthenticationPostprocessing() {
//		provider.setFinishWithFinalAuthentication
	}
	
	public void testNullDelegateAuthentication() {
		provider.setDelegateProvider(new AbstractTestProvider() {
			public Authentication authenticate(Authentication authentication)
					throws AuthenticationException {
				return null;
			}
		});
		assertEquals(null, provider.authenticate(new EmptyAuthenticationToken()));
	}
	
	private static abstract class AbstractTestProvider implements AuthenticationProvider {
		public boolean supports(Class<? extends Object> authentication) {
			return true;
		}
	}
}
