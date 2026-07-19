package com.payneteasy.superfly.security;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.ConfigAttribute;

/**
 * Test for {@link AccessDecisionManager} which delegates all the work to
 * another decision manager.
 * 
 * @author Roman Puchkovskiy
 */
public class DelegatingDecisionManagerTest {
    private AccessDecisionManager ourManager;
    private AccessDecisionManager delegate;

    @Before
    public void setUp() {
        delegate = EasyMock.createMock(AccessDecisionManager.class);
        ourManager = new DelegatingDecisionManager(delegate);
    }

    @Test
    public void testDelegateMethods() {
        EasyMock.expect(delegate.supports(EasyMock.anyObject(ConfigAttribute.class))).andReturn(true);
        EasyMock.replay(delegate);
        Assert.assertTrue(ourManager.supports(new ConfigAttribute() {
            private static final long serialVersionUID = 1L;

            public String getAttribute() {
                return "ha";
            }
        }));
        EasyMock.verify(delegate);

        EasyMock.reset(delegate);

        EasyMock.expect(delegate.supports(EasyMock.anyObject(Class.class))).andReturn(false);
        EasyMock.replay(delegate);
        Assert.assertFalse(ourManager.supports(String.class));
        EasyMock.verify(delegate);
    }
}
