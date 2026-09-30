package com.payneteasy.superfly.security.x509;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class X509PreAuthenticatedAuthenticationProviderTest {

    private X509PreAuthenticatedAuthenticationProvider provider;

    @Before
    public void setUp() {
        UserDetailsService service = username -> {
            if ("CN=alice".equals(username)) {
                return new User(username, "", AuthorityUtils.createAuthorityList("ROLE_X509"));
            }
            throw new UsernameNotFoundException(username);
        };
        provider = new X509PreAuthenticatedAuthenticationProvider(service);
    }

    @Test
    public void authenticate_knownSubject_loadsUserDetails() {
        Authentication result = provider.authenticate(new PreAuthenticatedAuthenticationToken("CN=alice", "cert"));

        assertTrue(result.isAuthenticated());
        assertEquals("CN=alice", ((User) result.getPrincipal()).getUsername());
        assertEquals("ROLE_X509", result.getAuthorities().iterator().next().getAuthority());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void authenticate_unknownSubject_isRejected() {
        provider.authenticate(new PreAuthenticatedAuthenticationToken("CN=bob", "cert"));
    }

    @Test
    public void authenticate_otherTokenType_isIgnored() {
        assertNull(provider.authenticate(new org.springframework.security.authentication.TestingAuthenticationToken("a", "b")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_nullUserDetailsService_isRejected() {
        new X509PreAuthenticatedAuthenticationProvider(null);
    }
}
