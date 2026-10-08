package com.payneteasy.superfly.web;

import com.payneteasy.startup.parameters.AStartupParameter;

/**
 * Settings of the embedded server, read from environment variables (or system properties of the same name).
 * Secrets are meant to come from the environment: Jetty and the JVM echo command-line arguments.
 */
public interface IStartSuperflyConfig {

    @AStartupParameter(name = "JETTY_PORT", value = "8080")
    int getJettyPort();

    /** -1 disables the TLS connector */
    @AStartupParameter(name = "JETTY_PORT_SSL", value = "-1")
    int getJettyPortSsl();

    @AStartupParameter(name = "JETTY_SSL_KEYSTORE_PATH", value = "")
    String getJettySslKeystorePath();

    @AStartupParameter(name = "JETTY_SSL_KEYSTORE_PASSWORD", value = "", maskVariable = true)
    String getJettySslKeystorePassword();

    @AStartupParameter(name = "JETTY_SSL_TRUSTSTORE_PATH", value = "")
    String getJettySslTruststorePath();

    @AStartupParameter(name = "JETTY_SSL_TRUSTSTORE_PASSWORD", value = "", maskVariable = true)
    String getJettySslTruststorePassword();

    @AStartupParameter(name = "JETTY_SSL_CLIENT_AUTH_REQUIRED", value = "false")
    boolean getSslClientAuthRequired();

    @AStartupParameter(name = "JETTY_MAX_THREADS", value = "200")
    int getJettyMaxThreads();

    @AStartupParameter(name = "JETTY_MIN_THREADS", value = "8")
    int getJettyMinThreads();

    @AStartupParameter(name = "JETTY_CONTEXT", value = "/")
    String getJettyContext();

    @AStartupParameter(name = "JETTY_OUTPUT_BUFFER_SIZE", value = "32768")
    int getOutputBufferSize();

    @AStartupParameter(name = "JETTY_HEADER_SIZE", value = "8192")
    int getHeaderSize();

    @AStartupParameter(name = "JETTY_SEND_SERVER_VERSION", value = "true")
    boolean isSendServerVersion();

    @AStartupParameter(name = "JETTY_SEND_DATE_HEADER", value = "true")
    boolean isSendDateHeader();

    @AStartupParameter(name = "JETTY_SECURE_SCHEME", value = "https")
    String getSecureScheme();

    @AStartupParameter(name = "JETTY_STOP_TIMEOUT_MS", value = "5000")
    long getJettyStopTimeoutMs();

    /**
     * X-Forwarded-* / Forwarded headers are trusted only on request: Jetty cannot restrict them to proxy
     * addresses, so enable it only when the port is reachable by the proxy alone.
     */
    @AStartupParameter(name = "JETTY_TRUST_FORWARDED", value = "false")
    boolean isTrustForwarded();

    /** Optional Jetty XML (ids "Server" and "wac") applied after the built-in setup; empty means none */
    @AStartupParameter(name = "JETTY_XML_CONFIG_FILE_PATH", value = "")
    String getJettyXmlConfigFilePath();

    @AStartupParameter(name = "DB_HOST", value = "mysql")
    String getDbHost();

    @AStartupParameter(name = "DB_PORT", value = "3306")
    int getDbPort();

    @AStartupParameter(name = "DB_NAME", value = "sso")
    String getDbName();

    @AStartupParameter(name = "DB_USER", value = "sso")
    String getDbUser();

    @AStartupParameter(name = "DB_PASSWORD", value = "", maskVariable = true)
    String getDbPassword();

    @AStartupParameter(name = "DB_TIMEZONE", value = "UTC")
    String getDbTimezone();
}
