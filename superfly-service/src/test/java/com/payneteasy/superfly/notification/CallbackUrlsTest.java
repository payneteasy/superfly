package com.payneteasy.superfly.notification;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * The callback URL is written to logs and exception messages without user info, query and fragment.
 *
 * <p>The same cases are checked for the URL in the error messages of {@code ApacheHC5HttpClient}
 * (superfly-httpclient-hc5, {@code ApacheHC5HttpClientUrlMaskingTest}): keep the two in line.
 */
public class CallbackUrlsTest {

    private static final String INVALID = "(invalid callback URL)";

    @Test
    public void httpAndHttpsUrlsWithoutExtrasAreKept() {
        assertMasked("http://subsystem.example/superfly-callback", "http://subsystem.example/superfly-callback");
        assertMasked("https://subsystem.example/superfly-callback", "https://subsystem.example/superfly-callback");
        assertMasked("https://subsystem.example", "https://subsystem.example");
        assertMasked("https://subsystem.example/a%20b/c", "https://subsystem.example/a%20b/c");
    }

    @Test
    public void userInfoIsRemoved() {
        assertMasked("https://callback-user:dummy-password@subsystem.example/superfly-callback",
                "https://subsystem.example/superfly-callback");
        assertMasked("https://callback-user@subsystem.example/superfly-callback",
                "https://subsystem.example/superfly-callback");
        assertMasked("http://callback%40user:dummy-password@subsystem.example/cb", "http://subsystem.example/cb");
    }

    @Test
    public void everythingBeforeTheLastAtSignOfTheAuthorityIsUserInfo() {
        assertMasked("http://callback-user@dummy-password@subsystem.example/cb", "http://subsystem.example/cb");
    }

    @Test
    public void atSignInThePathIsNotUserInfo() {
        assertMasked("https://subsystem.example/user@host/cb", "https://subsystem.example/user@host/cb");
        assertMasked("https://callback-user:dummy-password@subsystem.example/user@host/cb",
                "https://subsystem.example/user@host/cb");
    }

    @Test
    public void queryIsRemoved() {
        assertMasked("https://subsystem.example/superfly-callback?key=dummy-query-secret",
                "https://subsystem.example/superfly-callback");
        assertMasked("https://subsystem.example?key=dummy-query-secret", "https://subsystem.example");
        assertMasked("https://subsystem.example/superfly-callback?", "https://subsystem.example/superfly-callback");
    }

    @Test
    public void fragmentIsRemoved() {
        assertMasked("https://subsystem.example/superfly-callback#dummy-fragment",
                "https://subsystem.example/superfly-callback");
        assertMasked("https://subsystem.example/superfly-callback?key=dummy-query-secret#dummy-fragment",
                "https://subsystem.example/superfly-callback");
    }

    @Test
    public void portIsKept() {
        assertMasked("https://subsystem.example:8443/superfly-callback", "https://subsystem.example:8443/superfly-callback");
        assertMasked("http://callback-user:dummy-password@subsystem.example:8080/cb?key=dummy-query-secret#dummy-fragment",
                "http://subsystem.example:8080/cb");
    }

    @Test
    public void hostWithUnderscoreIsKept() {
        assertMasked("http://superfly_client:8080/cb", "http://superfly_client:8080/cb");
        assertMasked("http://superfly_client/cb", "http://superfly_client/cb");
        assertMasked("http://callback-user:dummy-password@superfly_client:8080/cb?key=dummy-query-secret#dummy-fragment",
                "http://superfly_client:8080/cb");
        assertMasked("https://callback-user@sub_system.example/cb", "https://sub_system.example/cb");
    }

    @Test
    public void ipv6HostIsKept() {
        assertMasked("http://[::1]:8080/cb", "http://[::1]:8080/cb");
        assertMasked("https://[2001:db8::1]/cb", "https://[2001:db8::1]/cb");
        assertMasked("https://callback-user:dummy-password@[2001:db8::1]:8443/cb?key=dummy-query-secret#dummy-fragment",
                "https://[2001:db8::1]:8443/cb");
    }

    @Test
    public void urlThatCannotBeParsedIsNotRepeated() {
        assertMasked("https://callback-user:dummy-password@subsystem.example/superfly callback?key=dummy-query-secret",
                INVALID);
        assertMasked("https://subsystem.example/cb{id}?key=dummy-query-secret#dummy fragment", INVALID);
        assertMasked("superfly_client:8080/cb?key=dummy-query-secret", INVALID);
    }

    @Test
    public void urlThatIsNotAbsoluteWithAHostIsNotRepeated() {
        assertMasked("", INVALID);
        assertMasked("/cb?key=dummy-query-secret", INVALID);
        assertMasked("//subsystem.example/cb?key=dummy-query-secret", INVALID);
        assertMasked("callback-user:dummy-password@subsystem.example/cb", INVALID);
        assertMasked("subsystem.example:8080/cb?key=dummy-query-secret", INVALID);
        assertMasked("mailto:callback-user@subsystem.example", INVALID);
        assertMasked("https:///cb?key=dummy-query-secret", INVALID);
        assertMasked("https://callback-user:dummy-password@/cb", INVALID);
    }

    @Test
    public void hostWithNonAsciiLettersIsKept() {
        assertMasked("http://m\u00fcnchen.example/cb", "http://m\u00fcnchen.example/cb");
        assertMasked("http://callback-user:dummy-password@m\u00fcnchen.example:8080/cb?key=dummy-query-secret",
                "http://m\u00fcnchen.example:8080/cb");
    }

    @Test
    public void atSignInTheQueryAfterThePathIsNotUserInfo() {
        assertMasked("https://subsystem.example/cb?email=user@subsystem.example", "https://subsystem.example/cb");
        assertMasked("https://callback-user:dummy-password@subsystem.example/cb?email=user@subsystem.example",
                "https://subsystem.example/cb");
    }

    /** A '/', '?' or '#' ends the authority, so the rest of the user info is taken for the host, path, query or fragment. */
    @Test
    public void userInfoWithASlashIsNotRepeated() {
        assertMasked("https://svc:pa/ss@cb.example/notify", INVALID);
        assertMasked("https://callback-user:dummy/password@subsystem.example/superfly-callback", INVALID);
    }

    @Test
    public void userInfoWithAHashIsNotRepeated() {
        assertMasked("https://svc:pa#ss@cb.example/n", INVALID);
        assertMasked("https://callback-user:dummy#password@subsystem.example/superfly-callback", INVALID);
        assertMasked("https://callback-user:1234#password@subsystem.example/superfly-callback", INVALID);
        assertMasked("https://callback-user#password@subsystem.example/superfly-callback", INVALID);
    }

    @Test
    public void userInfoWithAQuestionMarkIsNotRepeated() {
        assertMasked("https://callback-user:dummy?password@subsystem.example/superfly-callback", INVALID);
        assertMasked("https://callback-user:1234?password@subsystem.example/superfly-callback", INVALID);
        assertMasked("https://callback-user?password@subsystem.example/superfly-callback", INVALID);
    }

    /** It cannot be told from user info with a '?' in it. */
    @Test
    public void atSignInTheQueryOfAUrlWithoutPathIsNotRepeated() {
        assertMasked("https://subsystem.example?email=user@subsystem.example", INVALID);
    }

    @Test
    public void missingUrlIsShownAsNull() {
        assertEquals("null", CallbackUrls.masked(null));
    }

    private static void assertMasked(String url, String expected) {
        assertEquals(url, expected, CallbackUrls.masked(url));
    }
}
