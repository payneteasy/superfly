package com.payneteasy.superfly.web.spring.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

public class CspPolicyBuilderTest {

    private static final String BASE = "default-src 'self'; script-src 'self' 'unsafe-inline'; " +
            "style-src 'self' 'unsafe-inline'%s; img-src 'self' data:; frame-ancestors 'none'; form-action 'self'%s";

    @Test
    public void noUrlsGivesBasePolicy() {
        assertEquals(String.format(BASE, "", ""), CspPolicyBuilder.build(List.of(), List.of()));
    }

    @Test
    public void originsOfTwoSubsystemsGoToTheRightDirectives() {
        String csp = CspPolicyBuilder.build(
                List.of("https://b.example:8443/landing?x=1", "http://a.example/sub"),
                List.of("https://cdn.example/login.css"));
        assertEquals(String.format(BASE, " https://cdn.example", " http://a.example https://b.example:8443"), csp);
    }

    @Test
    public void duplicatesAreRemovedAndOrderIsStable() {
        String one = CspPolicyBuilder.build(List.of("https://b.example/x", "https://a.example/y", "HTTPS://B.example/z"), List.of());
        String two = CspPolicyBuilder.build(List.of("https://a.example/1", "https://b.example/2"), List.of());
        assertEquals(one, two);
        assertEquals(String.format(BASE, "", " https://a.example https://b.example"), one);
    }

    @Test
    public void garbageAndDangerousUrlsAreSkipped() {
        List<String> bad = List.of("", "   ", "not a url", "javascript:alert(1)", "ftp://a.example/", "data:text/html,x",
                "http://", "https://user:pw@a.example/", "http://a.example;script-src *", "http://a.example, *",
                "https://a.example:99999/", "https://*.example/", "https://a.example'/", "//a.example/");
        assertEquals(String.format(BASE, "", ""), CspPolicyBuilder.build(bad, bad));
    }

    @Test
    public void semicolonInPathDoesNotReachTheHeader() {
        String csp = CspPolicyBuilder.build(List.of("https://a.example/land;jsessionid=1"), List.of());
        assertEquals(String.format(BASE, "", " https://a.example"), csp);
        assertFalse(csp.contains("jsessionid"));
    }

    @Test
    public void toOriginHandlesNull() {
        assertNull(CspPolicyBuilder.toOrigin(null));
    }
}
