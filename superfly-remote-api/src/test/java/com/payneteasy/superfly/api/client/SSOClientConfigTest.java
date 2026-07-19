package com.payneteasy.superfly.api.client;

import com.payneteasy.http.client.api.HttpRequestParameters;
import com.payneteasy.http.client.api.HttpTimeouts;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

public class SSOClientConfigTest {

    private static final String BASE_URL       = "https://sso.example.com/api";
    private static final String SUBSYSTEM_NAME = "test-subsystem";

    @After
    public void tearDown() {
        System.clearProperty(SSOClientConfig.ALLOW_INSECURE_PROPERTY);
    }

    // ── parametersFor: default vs override ──────────────────────────────────

    @Test
    public void parametersFor_returnsDefault_whenNoOverride() {
        HttpRequestParameters def = HttpRequestParameters.builder()
                .timeouts(new HttpTimeouts(5_000, 30_000)).build();
        SSOClientConfig config = baseBuilder().defaultParameters(def).build();

        assertSame(def, config.parametersFor(Endpoint.AUTHENTICATE));
        assertSame(def, config.parametersFor(Endpoint.GET_EVENTS));
    }

    @Test
    public void parametersFor_returnsOverride_whenSet() {
        HttpRequestParameters def      = HttpRequestParameters.builder()
                .timeouts(new HttpTimeouts(5_000, 30_000)).build();
        HttpRequestParameters longPoll = HttpRequestParameters.builder()
                .timeouts(new HttpTimeouts(5_000, 90_000)).build();

        SSOClientConfig config = baseBuilder()
                .defaultParameters(def)
                .endpointParameter(Endpoint.GET_EVENTS, longPoll)
                .build();

        assertSame("override for GET_EVENTS",  longPoll, config.parametersFor(Endpoint.GET_EVENTS));
        assertSame("default for AUTHENTICATE", def,      config.parametersFor(Endpoint.AUTHENTICATE));
    }

    // ── urlFor ───────────────────────────────────────────────────────────────

    @Test
    public void urlFor_concatenatesBaseAndPath() {
        SSOClientConfig config = baseBuilder().build();
        assertEquals(BASE_URL + "/authenticate", config.urlFor(Endpoint.AUTHENTICATE));
        assertEquals(BASE_URL + "/getEvents",    config.urlFor(Endpoint.GET_EVENTS));
    }

    @Test
    public void urlFor_handlesTrailingSlash() {
        SSOClientConfig config = baseBuilder().baseUrl("https://sso.example.com/api/").build();
        assertEquals("https://sso.example.com/api", config.getBaseUrl());
        assertEquals("https://sso.example.com/api/authenticate", config.urlFor(Endpoint.AUTHENTICATE));
    }

    // ── HTTPS validation ────────────────────────────────────────────────────

    @Test
    public void httpUrl_rejectedByDefault() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () ->
                baseBuilder().baseUrl("http://insecure.example.com").build());
        assertNotNull(e.getMessage());
    }

    @Test
    public void httpUrl_acceptedWhenInsecurePropertySet() {
        System.setProperty(SSOClientConfig.ALLOW_INSECURE_PROPERTY, "true");
        SSOClientConfig config = baseBuilder().baseUrl("http://insecure.example.com").build();
        assertEquals("http://insecure.example.com", config.getBaseUrl());
    }

    // ── null/empty validation ────────────────────────────────────────────────

    @Test
    public void nullBaseUrl_throwsIAE() {
        assertThrows(IllegalArgumentException.class, () ->
                baseBuilder().baseUrl(null).build());
    }

    @Test
    public void emptyBaseUrl_throwsIAE() {
        assertThrows(IllegalArgumentException.class, () ->
                baseBuilder().baseUrl("").build());
    }

    @Test
    public void blankBaseUrl_throwsIAE() {
        assertThrows(IllegalArgumentException.class, () ->
                baseBuilder().baseUrl("   ").build());
    }

    @Test
    public void nullSubsystemName_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
                baseBuilder().subsystemName(null).build());
    }

    @Test
    public void nullDefaultParameters_throwsNPE() {
        assertThrows(NullPointerException.class, () ->
                SSOClientConfig.builder()
                        .baseUrl(BASE_URL)
                        .subsystemName(SUBSYSTEM_NAME)
                        .build());
    }

    @Test
    public void nullSubsystemToken_isAllowed() {
        SSOClientConfig config = baseBuilder().subsystemToken(null).build();
        assertNull(config.getSubsystemToken());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static SSOClientConfig.SSOClientConfigBuilder baseBuilder() {
        return SSOClientConfig.builder()
                .baseUrl(BASE_URL)
                .subsystemName(SUBSYSTEM_NAME)
                .subsystemToken("test-token")
                .defaultParameters(HttpRequestParameters.builder().build());
    }
}
