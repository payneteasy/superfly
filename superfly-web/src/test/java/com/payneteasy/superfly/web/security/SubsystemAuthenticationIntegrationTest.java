package com.payneteasy.superfly.web.security;

import com.payneteasy.superfly.security.x509.X509PreAuthenticatedAuthenticationProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Интеграционный тест для двух независимых каналов аутентификации:
 *
 * 1. Subsystem token (X-Subsystem-Name + X-Subsystem-Token):
 *    Converter → SubsystemAuthenticationProvider
 *    Проверяет имя subsystem + токен против БД.
 *
 * 2. X509 (client certificate):
 *    X509AuthenticationFilter извлекает CN из сертификата → PreAuthenticatedAuthenticationToken("cn", cert)
 *    → X509PreAuthenticatedAuthenticationProvider
 *    Проверяет CN против БД. Сам сертификат = доверие; дополнительный токен не нужен.
 *
 * Оба канала используют один UserDetailsService (SubsystemUserDetailsService в production).
 */
public class SubsystemAuthenticationIntegrationTest {

    private UserDetailsService userDetailsService;
    private AuthenticationManager authManager;

    @Before
    public void setUp() throws Exception {
        userDetailsService = createMock(UserDetailsService.class);

        SubsystemAuthenticationProvider subsystemProvider =
                new SubsystemAuthenticationProvider(userDetailsService);

        X509PreAuthenticatedAuthenticationProvider x509Provider =
                new X509PreAuthenticatedAuthenticationProvider(userDetailsService);
        x509Provider.afterPropertiesSet();

        authManager = new ProviderManager(List.of(subsystemProvider, x509Provider));
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── Subsystem token: Converter создаёт токен, Provider проверяет ─────────

    @Test
    public void testTokenAuth_validCredentials_authenticated() {
        expect(userDetailsService.loadUserByUsername("billing")).andReturn(user("billing", "s3cr3t"));
        replay(userDetailsService);

        Authentication result = authManager.authenticate(
                new SubsystemAuthenticationToken("billing", "s3cr3t"));

        assertTrue(result.isAuthenticated());
        assertEquals("Principal должен быть именем subsystem, не UserDetails", "billing", result.getPrincipal());
        assertNull("Токен должен быть сброшен после аутентификации", result.getCredentials());
        verify(userDetailsService);
    }

    @Test(expected = BadCredentialsException.class)
    public void testTokenAuth_wrongToken_rejected() {
        // Знаешь имя subsystem, но не знаешь токен — не пройдёшь
        expect(userDetailsService.loadUserByUsername("billing")).andReturn(user("billing", "s3cr3t"));
        replay(userDetailsService);

        authManager.authenticate(new SubsystemAuthenticationToken("billing", "wrong-token"));
    }

    @Test(expected = AuthenticationException.class)
    public void testTokenAuth_unknownSubsystem_rejected() {
        expect(userDetailsService.loadUserByUsername("unknown"))
                .andThrow(new UsernameNotFoundException("unknown subsystem"));
        replay(userDetailsService);

        authManager.authenticate(new SubsystemAuthenticationToken("unknown", "any-token"));
    }

    // ── X509: X509AuthenticationFilter → PreAuthenticatedAuthenticationToken ─

    @Test
    public void testX509Auth_knownCN_authenticated() {
        // В production: X509AuthenticationFilter извлекает CN из сертификата
        // и создаёт PreAuthenticatedAuthenticationToken("billing", <cert>).
        // Здесь эмулируем этот токен напрямую.
        expect(userDetailsService.loadUserByUsername("billing")).andReturn(user("billing", "stored-token"));
        replay(userDetailsService);

        Authentication result = authManager.authenticate(
                new PreAuthenticatedAuthenticationToken("billing", "<x509-cert-stub>"));

        assertTrue(result.isAuthenticated());
        assertEquals("billing", result.getName());
        verify(userDetailsService);
    }

    @Test(expected = AuthenticationException.class)
    public void testX509Auth_unknownCN_rejected() {
        // Сертификат с CN не из БД — отказ
        expect(userDetailsService.loadUserByUsername("rogue-cn"))
                .andThrow(new UsernameNotFoundException("subsystem not found"));
        replay(userDetailsService);

        authManager.authenticate(new PreAuthenticatedAuthenticationToken("rogue-cn", "<x509-cert-stub>"));
    }

    @Test
    public void testX509Token_doNotRouteToSubsystemProvider() {
        // PreAuthenticatedAuthenticationToken НЕ обрабатывается SubsystemAuthenticationProvider.
        // X509 токен идёт только через X509PreAuthenticatedAuthenticationProvider.
        expect(userDetailsService.loadUserByUsername("billing")).andReturn(user("billing", "stored-token"));
        replay(userDetailsService);

        Authentication x509Token = new PreAuthenticatedAuthenticationToken("billing", "<cert>");
        assertFalse("SubsystemProvider не должен принимать X509-токен",
                new SubsystemAuthenticationProvider(userDetailsService).supports(x509Token.getClass()));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private static UserDetails user(String username, String password) {
        return new User(username, password, List.of(new SimpleGrantedAuthority("ROLE_SUBSYSTEM")));
    }
}
