package com.payneteasy.superfly.security.processor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.security.AbstractSSOUserAwareTest;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;

import static org.junit.Assert.assertSame;

public class SSOUserShortCircuitingPostProcessorTest extends AbstractSSOUserAwareTest {
    private SSOUserShortCircuitingPostProcessor processor;

    @Before
    public void setUp() {
        processor = new SSOUserShortCircuitingPostProcessor();
    }

    @Test
    public void testDoNothing() {
        CompoundAuthentication request = new CompoundAuthentication();
        request.addReadyAuthentication(new SSOUserTransportAuthenticationToken(createSSOUser(2)));
        Authentication auth = processor.postProcess(request);
        assertSame(request, auth);

        request = new CompoundAuthentication();
        request.addReadyAuthentication(new SSOUserTransportAuthenticationToken(createSSOUser(1)));
        auth = processor.postProcess(request);
        assertSame(request, auth);
    }

    @Test
    public void testShortCircuit() {
        CompoundAuthentication request = new CompoundAuthentication();
        request.addReadyAuthentication(new SSOUserTransportAuthenticationToken(createSSOUser(1)));
        processor.setFinishWithSuperflyFinalAuthentication(true);
        Authentication auth = processor.postProcess(request);
        Assert.assertTrue("Got: " + auth.getClass(), auth instanceof SSOUserAuthenticationToken);
    }
}
