package com.payneteasy.superfly;

import com.payneteasy.superfly.web.SuperflyServer;

public class StartSuperflyServerTest {
    
    public static void main(String[] args) throws Exception {
        setTestSystemProperties();
        new SuperflyServer().startServer();
    }
    
    private static void setTestSystemProperties() {
        System.setProperty("JETTY_PORT", "-1");
        System.setProperty("JETTY_PORT_SSL", "-1");
        System.setProperty("JETTY_SSL_TRUSTSTORE_PATH", "config/truststore.jks");
        System.setProperty("JETTY_SSL_TRUSTSTORE_PASSWORD", "changeit");
        System.setProperty("JETTY_SSL_KEYSTORE_PATH", "config/keystore.jks");
        System.setProperty("JETTY_SSL_KEYSTORE_PASSWORD", "changeit");
        System.setProperty("JETTY_MAX_THREADS", "500");
        System.setProperty("JETTY_MIN_THREADS", "16");
        System.setProperty("JETTY_CONTEXT", "/superfly");
        System.setProperty("JETTY_OUTPUT_BUFFER_SIZE", "32768");
        System.setProperty("JETTY_HEADER_SIZE", "8192");
        System.setProperty("JETTY_SEND_SERVER_VERSION", "true");
        System.setProperty("JETTY_SEND_DATE_HEADER", "true");
        System.setProperty("JETTY_SECURE_SCHEME", "https");
        System.setProperty("JETTY_XML_CONFIG_FILE_PATH", "src/test/resources/jetty/jetty-test-config.xml");
        System.setProperty("JETTY_SSL_CLIENT_AUTH_REQUIRED", "true");
        System.setProperty("JETTY_STOP_TIMEOUT_MS", "5000");
    }
}
