package com.payneteasy.httpclient.contrib.ssl;

import org.easymock.EasyMock;
import org.junit.Test;

import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JdkSslCnVerifierTest {

    @Test
    public void cnWithEscapedCommaIsParsedAsSingleValue() throws Exception {
        SSLSession session = session("CN=a\\,b,O=Org", null);
        assertTrue(JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("a,b").verify("h", session));
        assertFalse(JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("a").verify("h", session));
    }

    @Test
    public void sanOnlyCertificateMatchesDnsName() throws Exception {
        Collection<List<?>> sans = List.of(List.of(2, "superfly-server"), List.of(7, "10.0.0.1"));
        SSLSession session = session("O=Org", sans);
        assertTrue(JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("superfly-server").verify("h", session));
        assertFalse(JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("10.0.0.1").verify("h", session));
        assertFalse(JdkSslSocketFactoryBuilder.buildCnHostnameVerifier("other").verify("h", session));
    }

    private static SSLSession session(String dn, Collection<List<?>> sans) throws Exception {
        X509Certificate cert = EasyMock.createMock(X509Certificate.class);
        expect(cert.getSubjectX500Principal()).andReturn(new X500Principal(dn)).anyTimes();
        expect(cert.getSubjectAlternativeNames()).andReturn(sans).anyTimes();
        replay(cert);
        SSLSession session = EasyMock.createMock(SSLSession.class);
        expect(session.getPeerCertificates()).andReturn(new Certificate[]{cert}).anyTimes();
        replay(session);
        return session;
    }
}
