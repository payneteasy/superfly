package com.payneteasy.superfly.security;

import org.easymock.EasyMock;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.ConfigAttribute;

import junit.framework.TestCase;

/**
 * {@link AccessDecisionManager} which delegates all the work to other
 * decision manager.
 * 
 * @author Roman Puchkovskiy
 */
public class DelegatingDecisionManagerTest extends TestCase {
	private AccessDecisionManager ourManager;
	private AccessDecisionManager delegate;
	
	public void setUp() {
		delegate = EasyMock.createMock(AccessDecisionManager.class);
		ourManager = new DelegatingDecisionManager(delegate);
	}
	
	public void testDelegateMethods() {
		EasyMock.expect(delegate.supports(EasyMock.anyObject(ConfigAttribute.class))).andReturn(true);
		EasyMock.replay(delegate);
		assertTrue(ourManager.supports(new ConfigAttribute() {
			private static final long serialVersionUID = 1L;
			public String getAttribute() {
				return "ha";
			}
		}));
		EasyMock.verify(delegate);
		
		EasyMock.reset(delegate);
		
		EasyMock.expect(delegate.supports(EasyMock.anyObject(Class.class))).andReturn(false);
		EasyMock.replay(delegate);
		assertFalse(ourManager.supports(String.class));
		EasyMock.verify(delegate);
	}
}
