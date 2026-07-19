package com.payneteasy.superfly.web.security;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * OWASP-ориентированные тесты для subsystem auth.
 *
 * A01 – Broken Access Control   : обход аутентификации, подбор
 * A02 – Cryptographic Failures  : timing-attack на сравнение токена
 * A03 – Injection               : header injection, log injection (CRLF)
 * A07 – Authentication Failures : пустые/blank credentials, граничные случаи
 * A09 – Security Logging        : CRLF не попадает в логи как новая строка
 */
public class SubsystemAuthenticationOWASPTest {

    private static final String VALID_NAME  = "billing";
    private static final String VALID_TOKEN = "s3cr3t-token";

    private SubsystemAuthenticationConverter  converter;
    private SubsystemAuthenticationProvider   provider;
    private UserDetailsService                userDetailsService;

    private Logger             converterLogger;
    private Logger             providerLogger;
    private ListAppender<ILoggingEvent> logCapture;

    @Before
    public void setUp() {
        userDetailsService = createMock(UserDetailsService.class);
        converter = new SubsystemAuthenticationConverter();
        provider  = new SubsystemAuthenticationProvider(userDetailsService);

        converterLogger = (Logger) LoggerFactory.getLogger(SubsystemAuthenticationConverter.class);
        providerLogger  = (Logger) LoggerFactory.getLogger(SubsystemAuthenticationProvider.class);

        logCapture = new ListAppender<>();
        logCapture.start();
        converterLogger.addAppender(logCapture);
        providerLogger.addAppender(logCapture);
        converterLogger.setLevel(Level.ALL);
        providerLogger.setLevel(Level.ALL);
    }

    @After
    public void tearDown() {
        converterLogger.detachAppender(logCapture);
        providerLogger.detachAppender(logCapture);
    }

    // ── A07: пустые и blank credentials ──────────────────────────────────────

    @Test
    public void testConverter_emptyName_returnsNull() {
        // Пустая строка != null, но должна отклоняться на входе (defence-in-depth)
        HttpServletRequest req = createMock(HttpServletRequest.class);
        expect(req.getHeader("X-Subsystem-Name")).andReturn("");
        expect(req.getHeader("X-Subsystem-Token")).andReturn(VALID_TOKEN);
        replay(req);

        assertNull("Пустое имя должно отклоняться в Converter", converter.convert(req));
        verify(req);
    }

    @Test
    public void testConverter_blankName_returnsNull() {
        HttpServletRequest req = createMock(HttpServletRequest.class);
        expect(req.getHeader("X-Subsystem-Name")).andReturn("   ");
        expect(req.getHeader("X-Subsystem-Token")).andReturn(VALID_TOKEN);
        replay(req);

        assertNull("Whitespace-only имя должно отклоняться в Converter", converter.convert(req));
        verify(req);
    }

    @Test
    public void testConverter_emptyToken_returnsNull() {
        HttpServletRequest req = createMock(HttpServletRequest.class);
        expect(req.getHeader("X-Subsystem-Name")).andReturn(VALID_NAME);
        expect(req.getHeader("X-Subsystem-Token")).andReturn("");
        replay(req);

        assertNull("Пустой токен должен отклоняться в Converter", converter.convert(req));
        verify(req);
    }

    @Test
    public void testConverter_blankToken_returnsNull() {
        HttpServletRequest req = createMock(HttpServletRequest.class);
        expect(req.getHeader("X-Subsystem-Name")).andReturn(VALID_NAME);
        expect(req.getHeader("X-Subsystem-Token")).andReturn("\t\t");
        replay(req);

        assertNull("Whitespace-only токен должен отклоняться в Converter", converter.convert(req));
        verify(req);
    }

    // ── A01: обход через похожие токены ──────────────────────────────────────

    @Test(expected = BadCredentialsException.class)
    public void testProvider_tokenWithLeadingSpace_rejected() {
        // " s3cr3t" != "s3cr3t" — пробел не должен быть проигнорирован
        expect(userDetailsService.loadUserByUsername(VALID_NAME)).andReturn(user(VALID_NAME, VALID_TOKEN));
        replay(userDetailsService);

        provider.authenticate(new SubsystemAuthenticationToken(VALID_NAME, " " + VALID_TOKEN));
    }

    @Test(expected = BadCredentialsException.class)
    public void testProvider_tokenWithTrailingNewline_rejected() {
        // "s3cr3t\n" != "s3cr3t"
        expect(userDetailsService.loadUserByUsername(VALID_NAME)).andReturn(user(VALID_NAME, VALID_TOKEN));
        replay(userDetailsService);

        provider.authenticate(new SubsystemAuthenticationToken(VALID_NAME, VALID_TOKEN + "\n"));
    }

    @Test(expected = BadCredentialsException.class)
    public void testProvider_uppercaseToken_rejected() {
        // Токены case-sensitive
        expect(userDetailsService.loadUserByUsername(VALID_NAME)).andReturn(user(VALID_NAME, VALID_TOKEN));
        replay(userDetailsService);

        provider.authenticate(new SubsystemAuthenticationToken(VALID_NAME, VALID_TOKEN.toUpperCase()));
    }

    // ── A02: timing-safe comparison ──────────────────────────────────────────

    @Test(expected = BadCredentialsException.class)
    public void testProvider_shortToken_rejected() {
        // Короткий токен (разная длина) — тоже отклоняется
        expect(userDetailsService.loadUserByUsername(VALID_NAME)).andReturn(user(VALID_NAME, VALID_TOKEN));
        replay(userDetailsService);

        provider.authenticate(new SubsystemAuthenticationToken(VALID_NAME, "short"));
    }

    @Test(expected = BadCredentialsException.class)
    public void testProvider_longToken_rejected() {
        // Токен той же длины что и правильный, но другой — должен отклоняться
        String sameLength = VALID_TOKEN.replace('s', 'x');
        expect(userDetailsService.loadUserByUsername(VALID_NAME)).andReturn(user(VALID_NAME, VALID_TOKEN));
        replay(userDetailsService);

        provider.authenticate(new SubsystemAuthenticationToken(VALID_NAME, sameLength));
    }

    @Test(expected = BadCredentialsException.class)
    public void testProvider_emptyStoredToken_rejected() {
        // Если в БД пустой токен — запрос с пустым токеном всё равно отклоняется
        // (пустой токен отсеивается в Converter; до Provider дойдёт только если
        // создан программно; в любом случае MessageDigest.isEqual("","") = true,
        // но getPassword()==null guard должен это поймать)
        expect(userDetailsService.loadUserByUsername(VALID_NAME)).andReturn(user(VALID_NAME, null));
        replay(userDetailsService);

        provider.authenticate(new SubsystemAuthenticationToken(VALID_NAME, ""));
    }

    // ── A03/A09: CRLF log injection ───────────────────────────────────────────

    @Test
    public void testConverter_crlfInName_notInjectedInLog() {
        String maliciousName = "billing\r\nFAKE_LOG_ENTRY auth=success subsystem=admin";

        HttpServletRequest req = createMock(HttpServletRequest.class);
        expect(req.getHeader("X-Subsystem-Name")).andReturn(maliciousName);
        expect(req.getHeader("X-Subsystem-Token")).andReturn(VALID_TOKEN);
        replay(req);

        converter.convert(req);

        // Ни в одной строке лога CRLF не должен присутствовать в сыром виде
        for (ILoggingEvent event : logCapture.list) {
            String msg = event.getFormattedMessage();
            assertFalse("CRLF не должен проходить в лог как разрыв строки: " + msg,
                    msg.contains("\r") || msg.contains("\n"));
            assertFalse("Инжектированный текст не должен попасть в лог: " + msg,
                    msg.contains("FAKE_LOG_ENTRY"));
        }
        verify(req);
    }

    @Test
    public void testProvider_crlfInPrincipal_notInjectedInLog() {
        String maliciousName = "billing\r\nFAKE_LOG_ENTRY level=ERROR";

        expect(userDetailsService.loadUserByUsername(maliciousName))
                .andReturn(user(maliciousName, VALID_TOKEN));
        replay(userDetailsService);

        provider.authenticate(new SubsystemAuthenticationToken(maliciousName, VALID_TOKEN));

        for (ILoggingEvent event : logCapture.list) {
            String msg = event.getFormattedMessage();
            assertFalse("CRLF не должен проходить в лог: " + msg,
                    msg.contains("\r") || msg.contains("\n"));
        }
        verify(userDetailsService);
    }

    // ── A09: токен не должен появляться в логах ───────────────────────────────

    @Test
    public void testProvider_tokenNotInLogs() {
        expect(userDetailsService.loadUserByUsername(VALID_NAME)).andReturn(user(VALID_NAME, VALID_TOKEN));
        replay(userDetailsService);

        provider.authenticate(new SubsystemAuthenticationToken(VALID_NAME, VALID_TOKEN));

        for (ILoggingEvent event : logCapture.list) {
            assertFalse("Токен не должен попасть в лог: " + event.getFormattedMessage(),
                    event.getFormattedMessage().contains(VALID_TOKEN));
        }
        verify(userDetailsService);
    }

    @Test(expected = BadCredentialsException.class)
    public void testProvider_wrongTokenNotInLogs() {
        String wrongToken = "leaked-wrong-token";
        expect(userDetailsService.loadUserByUsername(VALID_NAME)).andReturn(user(VALID_NAME, VALID_TOKEN));
        replay(userDetailsService);

        try {
            provider.authenticate(new SubsystemAuthenticationToken(VALID_NAME, wrongToken));
        } finally {
            for (ILoggingEvent event : logCapture.list) {
                assertFalse("Неверный токен тоже не должен попасть в лог: " + event.getFormattedMessage(),
                        event.getFormattedMessage().contains(wrongToken));
            }
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static UserDetails user(String username, String password) {
        return new User(username, password != null ? password : "",
                password != null,
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_SUBSYSTEM")));
    }
}
