package com.payneteasy.httpclient.contrib.ssl;

import org.easymock.EasyMock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class JdkSslSocketFactoryBuilderTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    // ── buildSocketFactory ────────────────────────────────────────────────────

    @Test
    public void testBuildSocketFactory_withBothStores_returnsNonNull() throws Exception {
        URL ksUrl = emptyJks("ks");
        URL tsUrl = emptyJks("ts");

        SSLSocketFactory factory = JdkSslSocketFactoryBuilder.buildSocketFactory(
                ksUrl, "testpass", tsUrl, "testpass");

        assertNotNull("SSLSocketFactory must not be null", factory);
    }

    @Test
    public void testBuildSocketFactory_nullKeyStore_doesNotThrow() throws Exception {
        URL tsUrl = emptyJks("ts");

        SSLSocketFactory factory = JdkSslSocketFactoryBuilder.buildSocketFactory(
                null, null, tsUrl, "testpass");

        assertNotNull(factory);
    }

    @Test
    public void testBuildSocketFactory_nullTrustStore_doesNotThrow() throws Exception {
        URL ksUrl = emptyJks("ks");

        SSLSocketFactory factory = JdkSslSocketFactoryBuilder.buildSocketFactory(
                ksUrl, "testpass", null, null);

        assertNotNull(factory);
    }

    @Test
    public void testBuildSocketFactory_bothNull_doesNotThrow() throws Exception {
        SSLSocketFactory factory = JdkSslSocketFactoryBuilder.buildSocketFactory(
                null, null, null, null);

        assertNotNull(factory);
    }

    @Test(expected = IOException.class)
    public void testBuildSocketFactory_nonExistentKeyStoreUrl_throwsIOException() throws Exception {
        URL bad = new File(tmp.getRoot(), "does-not-exist.jks").toURI().toURL();

        JdkSslSocketFactoryBuilder.buildSocketFactory(bad, "pass", null, null);
    }

    // ── buildSslContext ───────────────────────────────────────────────────────

    @Test
    public void testBuildSslContext_withBothStores_returnsNonNull() throws Exception {
        URL ksUrl = emptyJks("ks");
        URL tsUrl = emptyJks("ts");

        SSLContext ctx = JdkSslSocketFactoryBuilder.buildSslContext(ksUrl, "testpass", tsUrl, "testpass");

        assertNotNull("SSLContext must not be null", ctx);
    }

    @Test
    public void testBuildSslContext_nullKeyStore_doesNotThrow() throws Exception {
        URL tsUrl = emptyJks("ts");

        SSLContext ctx = JdkSslSocketFactoryBuilder.buildSslContext(null, null, tsUrl, "testpass");

        assertNotNull(ctx);
    }

    @Test
    public void testBuildSslContext_bothNull_doesNotThrow() throws Exception {
        SSLContext ctx = JdkSslSocketFactoryBuilder.buildSslContext(null, null, null, null);

        assertNotNull(ctx);
    }

    @Test
    public void testBuildSslContext_socketFactoryConsistent() throws Exception {
        // buildSocketFactory and buildSslContext must produce equivalent contexts
        URL ksUrl = emptyJks("ks2");
        URL tsUrl = emptyJks("ts2");

        SSLContext ctx = JdkSslSocketFactoryBuilder.buildSslContext(ksUrl, "testpass", tsUrl, "testpass");
        SSLSocketFactory factoryFromCtx = ctx.getSocketFactory();

        assertNotNull("SocketFactory from SSLContext must not be null", factoryFromCtx);
    }

    // ── buildTrustManager ────────────────────────────────────────────────────

    @Test
    public void testBuildTrustManager_withTrustStore_returnsNonNull() throws Exception {
        URL tsUrl = emptyJks("ts");

        X509TrustManager tm = JdkSslSocketFactoryBuilder.buildTrustManager(tsUrl, "testpass");

        assertNotNull("TrustManager must not be null", tm);
    }

    @Test
    public void testBuildTrustManager_nullUrl_returnsNull() throws Exception {
        X509TrustManager tm = JdkSslSocketFactoryBuilder.buildTrustManager(null, null);

        assertNull(tm);
    }

    // ── buildCnHostnameVerifier ───────────────────────────────────────────────

    @Test
    public void testBuildCnHostnameVerifier_matchingCn_returnsTrue() throws Exception {
        HostnameVerifier verifier = JdkSslSocketFactoryBuilder.
                buildCnHostnameVerifier("superfly-server");

        boolean result = verifier.verify("localhost", sessionWithCn("superfly-server"));

        assertTrue("Verifier must accept cert with matching CN", result);
    }

    @Test
    public void testBuildCnHostnameVerifier_nonMatchingCn_returnsFalse() throws Exception {
        HostnameVerifier verifier = JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-server");

        boolean result = verifier.verify("localhost", sessionWithCn("other-server"));

        assertFalse("Verifier must reject cert with non-matching CN", result);
    }

    @Test
    public void testBuildCnHostnameVerifier_emptyCnList_returnsFalse() throws Exception {
        HostnameVerifier verifier = JdkSslSocketFactoryBuilder.buildCnHostnameVerifier();

        boolean result = verifier.verify("localhost", sessionWithCn("superfly-server"));

        assertFalse("Verifier with empty allowed list must reject any CN", result);
    }

    @Test
    public void testBuildCnHostnameVerifier_multipleCns_acceptsEitherOne() throws Exception {
        HostnameVerifier verifier = JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-server", "superfly-dev");

        assertTrue(verifier.verify("localhost", sessionWithCn("superfly-server")));
        assertTrue(verifier.verify("localhost", sessionWithCn("superfly-dev")));
        assertFalse(verifier.verify("localhost", sessionWithCn("other")));
    }

    private SSLSession sessionWithCn(String cn) throws Exception {
        X509Certificate cert = EasyMock.createMock(X509Certificate.class);
        expect(cert.getSubjectX500Principal()).andReturn(new X500Principal("CN=" + cn)).anyTimes();
        replay(cert);

        SSLSession session = EasyMock.createMock(SSLSession.class);
        expect(session.getPeerCertificates()).andReturn(new Certificate[]{ cert }).anyTimes();
        replay(session);

        return session;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private URL emptyJks(String prefix) throws GeneralSecurityException, IOException {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        File f = tmp.newFile(prefix + ".jks");
        try (FileOutputStream fos = new FileOutputStream(f)) {
            ks.store(fos, "testpass".toCharArray());
        }
        return f.toURI().toURL();
    }
}
