package com.payneteasy.superfly.web;

import com.payneteasy.startup.parameters.AStartupParameter;

public interface IStartSuperflyConfig {

    @AStartupParameter(
            name = "JETTY_PORT",
            value = "-1"
    )
    int getJettyPort();

    @AStartupParameter(
            name = "JETTY_PORT_SSL",
            value = "-1"
    )
    int getJettyPortSsl();

    @AStartupParameter(
            name = "JETTY_SSL_TRUSTSTORE_PATH",
            value = "config/truststore.jks"
    )
    String getJettySslTruststorePath();

    @AStartupParameter(
            name = "JETTY_SSL_TRUSTSTORE_PASSWORD",
            value = "changeit",
            maskVariable = true
    )
    String getJettySslTruststorePassword();

    @AStartupParameter(
            name = "JETTY_SSL_KEYSTORE_PATH",
            value = "config/keystore.jks"
    )
    String getJettySslKeystorePath();

    @AStartupParameter(
            name = "JETTY_SSL_KEYSTORE_PASSWORD",
            value = "changeit",
            maskVariable = true
    )
    String getJettySslKeystorePassword();


    @AStartupParameter(
            name = "JETTY_MAX_THREADS",
            value = "500"
    )
    int getJettyMaxThreads();

    @AStartupParameter(
            name = "JETTY_MIN_THREADS",
            value = "16"
    )
    int getJettyMinThreads();

    @AStartupParameter(
            name = "JETTY_CONTEXT",
            value = "/superfly"
    )
    String getJettyContext();

    @AStartupParameter(
            name = "JETTY_OUTPUT_BUFFER_SIZE",
            value = "32768"
    )
    int getOutputBufferSize();

    @AStartupParameter(
            name = "JETTY_HEADER_SIZE",
            value = "8192"
    )
    int getHeaderSize();

    @AStartupParameter(
            name = "JETTY_SEND_SERVER_VERSION",
            value = "true"
    )
    boolean isSendServerVersion();

    @AStartupParameter(
            name = "JETTY_SEND_DATE_HEADER",
            value = "true"
    )
    boolean isSendDateHeader();

    @AStartupParameter(
            name = "JETTY_SECURE_SCHEME",
            value = "https"
    )
    String getSecureScheme();

    @AStartupParameter(
            name = "JETTY_OVERRIDE_WEB_DESCRIPTORS",
            value = ""
    )
    String getOverrideWebDescriptors();

    @AStartupParameter(
            name = "JETTY_SSL_CLIENT_AUTH_REQUIRED",
            value = "true"
    )
    boolean getSslClientAuthRequired();


    @AStartupParameter(
            name = "JETTY_STOP_TIMEOUT_MS",
            value = "5000"
    )
    long getJettyStopTimeoutMs();

}
