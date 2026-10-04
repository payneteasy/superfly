package com.payneteasy.superfly.web.spring.security;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * A login form stylesheet over plain http can be replaced by a MITM, so only https origins reach style-src.
 */
public class CspStyleSrcHttpsOnlyTest {

    private static final String BASE = "default-src 'self'; script-src 'self' 'unsafe-inline'; " +
            "style-src 'self' 'unsafe-inline'%s; img-src 'self' data:; frame-ancestors 'none'; form-action 'self'%s";

    @Test
    public void httpStyleOriginIsIgnored() {
        String csp = CspPolicyBuilder.build(List.of(), List.of("http://css.example/login.css"));
        assertEquals(String.format(BASE, "", ""), csp);
    }

    @Test
    public void httpsStyleOriginIsKeptNextToIgnoredHttpOne() {
        String csp = CspPolicyBuilder.build(List.of(), List.of("http://a.example/x.css", "HTTPS://b.example/y.css"));
        assertEquals(String.format(BASE, " https://b.example", ""), csp);
    }

    @Test
    public void httpFormActionOriginIsStillAllowed() {
        String csp = CspPolicyBuilder.build(List.of("http://a.example/landing"), List.of());
        assertEquals(String.format(BASE, "", " http://a.example"), csp);
    }
}
