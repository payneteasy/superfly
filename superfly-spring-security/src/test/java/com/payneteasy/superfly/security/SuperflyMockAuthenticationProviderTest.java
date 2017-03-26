package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.exception.StepTwoException;
import com.payneteasy.superfly.security.mapbuilder.ActionsMapBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class SuperflyMockAuthenticationProviderTest extends AbstractSuperflyAuthenticationProviderTest {

    private SuperflyMockAuthenticationProvider provider;

    @Before
    public void setUp() {
        provider = new SuperflyMockAuthenticationProvider();
        provider.setEnabled(true);
        provider.setUsername("pete");
        provider.setPassword("secret");
        provider.setActionsMapBuilder(new ActionsMapBuilder() {
            public Map<SSORole, SSOAction[]> build() throws Exception {
                return Collections.singletonMap(createSSORole(), new SSOAction[]{});
            }
        });
    }

    @Test
    public void testStep1Success() {
        assertNotNull(provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "secret", null)));
    }

    @Test
    public void testStep1_5Success() {
        try {
            provider.authenticate(new SSOUserTransportAuthenticationToken(createSSOUserWithOneRole()));
            fail();
        } catch (StepTwoException e) {
            // expected
        }
    }

    @Test
    public void testStep2Success() {
        assertNotNull(provider.authenticate(new SSOUserAndSelectedRoleAuthenticationToken(createSSOUserWithOneRole(), createSSORole())));
    }

    @Test
    public void testBadPassword() {
        try {
            provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("cory", "whatisthepassword", null));
            fail();
        } catch (BadCredentialsException e) {
            // expected
        }
    }

    @Test
    public void testNoRoles() {
        provider.setActionsMapBuilder(new ActionsMapBuilder() {
            public Map<SSORole, SSOAction[]> build() throws Exception {
                return Collections.emptyMap();
            }
        });
        try {
            provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("cory", "whatisthepassword", null));
            fail();
        } catch (BadCredentialsException e) {
            // expected
        }
    }

    @Test
    public void testDisabled() {
        provider.setEnabled(false);
        assertNull(provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "secret", null)));
    }
}
