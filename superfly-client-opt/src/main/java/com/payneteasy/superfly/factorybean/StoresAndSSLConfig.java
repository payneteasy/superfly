package com.payneteasy.superfly.factorybean;

import org.springframework.core.io.Resource;

/**
 * Config for AuthSSLProtocolSocketFactory.
 * 
 * @author Roman Puchkovskiy
 */
public class StoresAndSSLConfig {
    private Resource trustKeyStoreResource = null;
    private String trustKeyStorePassword;
    private Resource keyStoreResource = null;
    private String keyStorePassword;
    private String secureSchema = "https";
    private String host;
    private int securePort = 443;

    public Resource getTrustKeyStoreResource() {
        return trustKeyStoreResource;
    }

    public void setTrustKeyStoreResource(Resource trustKeyStoreResource) {
        this.trustKeyStoreResource = trustKeyStoreResource;
    }

    public String getTrustKeyStorePassword() {
        return trustKeyStorePassword;
    }

    public void setTrustKeyStorePassword(String trustKeyStorePassword) {
        this.trustKeyStorePassword = trustKeyStorePassword;
    }

    public Resource getKeyStoreResource() {
        return keyStoreResource;
    }

    public void setKeyStoreResource(Resource keyStoreResource) {
        this.keyStoreResource = keyStoreResource;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getSecureSchema() {
        return secureSchema;
    }

    public void setSecureSchema(String secureSchema) {
        this.secureSchema = secureSchema;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getSecurePort() {
        return securePort;
    }

    public void setSecurePort(int securePort) {
        this.securePort = securePort;
    }
}
