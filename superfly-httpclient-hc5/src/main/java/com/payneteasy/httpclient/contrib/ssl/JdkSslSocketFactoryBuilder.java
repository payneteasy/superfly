package com.payneteasy.httpclient.contrib.ssl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Builds JDK-native {@link SSLSocketFactory} and {@link X509TrustManager} from keyStore/trustStore URLs.
 * Unlike the former commons-httpclient {@code AuthSSLProtocolSocketFactory}, this builder has no
 * servlet-container or legacy-HTTP dependency and feeds the ApacheHC5HttpClient SSL context directly.
 */
public class JdkSslSocketFactoryBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(JdkSslSocketFactoryBuilder.class);

    private JdkSslSocketFactoryBuilder() {}

    /**
     * Builds an {@link SSLSocketFactory} from a keyStore and a trustStore.
     * Either URL may be {@code null}: {@code null} keyStore skips client auth setup;
     * {@code null} trustStore uses the JVM default trust anchors.
     */
    public static SSLSocketFactory buildSocketFactory(
            URL keyStoreUrl, String keyStorePassword,
            URL trustStoreUrl, String trustStorePassword
    ) throws GeneralSecurityException, IOException {
        LOG.debug("Building SSLSocketFactory: keyStore={}, trustStore={}", keyStoreUrl, trustStoreUrl);
        SSLContext ctx = buildSslContext(keyStoreUrl, keyStorePassword, trustStoreUrl, trustStorePassword);
        return ctx.getSocketFactory();
    }

    /**
     * Builds the first {@link X509TrustManager} from the given trustStore.
     * Returns {@code null} when {@code trustStoreUrl} is {@code null}.
     */
    public static X509TrustManager buildTrustManager(
            URL trustStoreUrl, String trustStorePassword
    ) throws GeneralSecurityException, IOException {
        if (trustStoreUrl == null) {
            LOG.debug("trustStoreUrl is null — returning null trust manager");
            return null;
        }
        LOG.debug("Building X509TrustManager from trustStore={}", trustStoreUrl);
        KeyStore ks = loadKeyStore(trustStoreUrl, trustStorePassword);
        TrustManager[] managers = buildTrustManagers(ks);
        for (TrustManager m : managers) {
            if (m instanceof X509TrustManager) {
                return (X509TrustManager) m;
            }
        }
        return null;
    }

    /**
     * Returns a {@link HostnameVerifier} that validates the server certificate's CN against
     * the provided set of expected values instead of the connection hostname.
     * <p>
     * Use when the server cert CN doesn't match the hostname (e.g. CN=superfly-server, host=localhost).
     * Safe only when paired with a custom trustStore — the cert chain is already verified by TLS.
     */
    public static HostnameVerifier buildCnHostnameVerifier(String... expectedCns) {
        Set<String> allowed = new HashSet<>(Arrays.asList(expectedCns));
        return (hostname, session) -> {
            try {
                java.security.cert.Certificate[] certs = session.getPeerCertificates();
                if (certs.length > 0 && certs[0] instanceof X509Certificate) {
                    X509Certificate cert = (X509Certificate) certs[0];
                    String dn = cert.getSubjectX500Principal().getName();
                    String cn = extractCn(dn);
                    boolean ok = cn != null && allowed.contains(cn);
                    LOG.debug("CN hostname check: host={}, cert-cn={}, allowed={}, result={}", hostname, cn, allowed, ok);
                    return ok;
                }
            } catch (Exception e) {
                LOG.warn("CN hostname verification failed: {}", e.getMessage());
            }
            return false;
        };
    }

    private static String extractCn(String dn) {
        for (String part : dn.split(",")) {
            String trimmed = part.trim();
            if (trimmed.startsWith("CN=")) {
                return trimmed.substring(3);
            }
        }
        return null;
    }

    /**
     * Builds an {@link SSLContext} from a keyStore and a trustStore.
     * Either URL may be {@code null}: {@code null} keyStore skips client auth setup;
     * {@code null} trustStore uses the JVM default trust anchors.
     * <p>
     * Use this method when the caller needs an {@link SSLContext} directly (e.g. Apache HttpClient 5)
     * rather than a {@link SSLSocketFactory}.
     */
    public static SSLContext buildSslContext(
            URL keyStoreUrl, String keyStorePassword,
            URL trustStoreUrl, String trustStorePassword
    ) throws GeneralSecurityException, IOException {
        KeyManager[] keyManagers = null;
        if (keyStoreUrl != null) {
            KeyStore ks = loadKeyStore(keyStoreUrl, keyStorePassword);
            keyManagers = buildKeyManagers(ks, keyStorePassword);
        }

        TrustManager[] trustManagers = null;
        if (trustStoreUrl != null) {
            KeyStore ts = loadKeyStore(trustStoreUrl, trustStorePassword);
            trustManagers = buildTrustManagers(ts);
        }

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(keyManagers, trustManagers, null);
        LOG.debug("SSLContext initialised successfully");
        return ctx;
    }

    private static KeyStore loadKeyStore(URL url, String password) throws GeneralSecurityException, IOException {
        LOG.debug("Loading KeyStore from {}", url);
        KeyStore ks = KeyStore.getInstance("JKS");
        try (InputStream is = url.openStream()) {
            ks.load(is, password != null ? password.toCharArray() : null);
        }
        return ks;
    }

    private static KeyManager[] buildKeyManagers(KeyStore ks, String password)
            throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password != null ? password.toCharArray() : null);
        KeyManager[] managers = kmf.getKeyManagers();
        for (int i = 0; i < managers.length; i++) {
            if (managers[i] instanceof X509KeyManager) {
                managers[i] = new AuthSSLX509KeyManager((X509KeyManager) managers[i]);
            }
        }
        return managers;
    }

    private static TrustManager[] buildTrustManagers(KeyStore ks)
            throws KeyStoreException, NoSuchAlgorithmException {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ks);
        TrustManager[] managers = tmf.getTrustManagers();
        for (int i = 0; i < managers.length; i++) {
            if (managers[i] instanceof X509TrustManager) {
                managers[i] = new AuthSSLX509TrustManager((X509TrustManager) managers[i]);
            }
        }
        return managers;
    }
}
