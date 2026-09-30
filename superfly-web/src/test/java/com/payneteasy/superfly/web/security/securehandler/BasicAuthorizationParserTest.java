package com.payneteasy.superfly.web.security.securehandler;

import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Base64;

import static org.junit.Assert.assertEquals;

/**
 * Contract for {@link BasicAuthorizationParser}.
 *
 * <p>Locks in decode behavior after the migration from {@code org.apache.commons.ssl.Base64}
 * (lenient) to the strict {@link java.util.Base64} decoder. The header carries a
 * {@code subsystem:token} credential, not {@code user:password}.
 */
public class BasicAuthorizationParserTest {

    private final BasicAuthorizationParser parser = new BasicAuthorizationParser();

    @Test
    public void parsesValidBasicHeader() throws AuthorizationException {
        AuthorizationBearer bearer = parser.parse(basic("subsystem-a:token-xyz"));

        assertEquals("subsystem-a", bearer.subsystem);
        assertEquals("token-xyz", bearer.token);
    }

    @Test
    public void decodesUsingDefaultCharset() throws AuthorizationException {
        // round-trips through Charset.defaultCharset(), matching the parser's own decode charset
        AuthorizationBearer bearer = parser.parse(basic("süb:tökën"));

        assertEquals("süb", bearer.subsystem);
        assertEquals("tökën", bearer.token);
    }

    @Test(expected = AuthorizationException.class)
    public void rejectsNonBasicScheme() throws AuthorizationException {
        parser.parse("Bearer " + encode("subsystem:token"));
    }

    @Test(expected = AuthorizationException.class)
    public void rejectsMissingCredentialPart() throws AuthorizationException {
        parser.parse("Basic");
    }

    @Test(expected = AuthorizationException.class)
    public void rejectsCredentialWithoutColon() throws AuthorizationException {
        parser.parse(basic("no-colon-here"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsMalformedBase64() throws AuthorizationException {
        // strict java.util.Base64 throws on illegal characters; the lenient commons.ssl
        // decoder used to swallow them — this test guards against silently reverting
        parser.parse("Basic !!!not-base64!!!");
    }

    @Test
    public void truncatesTokenContainingColon() throws AuthorizationException {
        // KNOWN LIMITATION: Tokenizer is backed by StringTokenizer, which splits on every ':',
        // so a token containing ':' is truncated at the first one. Pre-existing behavior,
        // documented here so any future fix updates this assertion deliberately.
        AuthorizationBearer bearer = parser.parse(basic("subsystem:to:ken"));

        assertEquals("subsystem", bearer.subsystem);
        assertEquals("to", bearer.token);
    }

    private static String basic(String credential) {
        return "Basic " + encode(credential);
    }

    private static String encode(String credential) {
        return Base64.getEncoder().encodeToString(credential.getBytes(Charset.defaultCharset()));
    }
}
