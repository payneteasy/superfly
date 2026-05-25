package com.payneteasy.httpclient.contrib.ssl;

import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class AuthSSLProtocolSocketFactoryTest {

    @Test
    public void testDefaultEnabledProtocolsAreTls12AndTls13() throws Exception {
        Field field = AuthSSLProtocolSocketFactory.class.getDeclaredField("DEFAULT_ENABLED_PROTOCOLS");
        field.setAccessible(true);
        String[] protocols = (String[]) field.get(null);

        assertArrayEquals(new String[]{"TLSv1.2", "TLSv1.3"}, protocols);
    }

    @Test
    public void testDefaultProtocolsDoNotContainSslOrTls10OrTls11() throws Exception {
        Field field = AuthSSLProtocolSocketFactory.class.getDeclaredField("DEFAULT_ENABLED_PROTOCOLS");
        field.setAccessible(true);
        String[] protocols = (String[]) field.get(null);

        for (String protocol : protocols) {
            assertFalse("Default protocols must not contain SSL: " + protocol, protocol.startsWith("SSL"));
            assertFalse("TLSv1.0 must not be in defaults: " + protocol, protocol.equals("TLSv1") || protocol.equals("TLSv1.0"));
            assertFalse("TLSv1.1 must not be in defaults: " + protocol, protocol.equals("TLSv1.1"));
        }
    }
}
