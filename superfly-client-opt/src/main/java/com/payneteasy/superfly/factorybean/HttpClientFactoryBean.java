package com.payneteasy.superfly.factorybean;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.springframework.beans.factory.FactoryBean;

import com.payneteasy.httpclient.contrib.ssl.AuthSSLProtocolSocketFactory;

/**
 * Factory bean for HttpClient.
 * 
 * @author Roman Puchkovskiy
 */
public class HttpClientFactoryBean implements FactoryBean {
	
	private boolean authenticationPreemptive = false;
	private String username;
	private String password;
	private String authHost;
	private StoresAndSSLConfig hostConfig = null;
	
	private HttpClient httpClient = null;
	
	public boolean isAuthenticationPreemptive() {
		return authenticationPreemptive;
	}

	public void setAuthenticationPreemptive(boolean authenticationPreemptive) {
		this.authenticationPreemptive = authenticationPreemptive;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAuthHost() {
		return authHost;
	}

	public void setAuthHost(String authHost) {
		this.authHost = authHost;
	}

	public void setHostConfig(StoresAndSSLConfig hostConfig) {
		this.hostConfig = hostConfig;
	}

	public synchronized Object getObject() throws Exception {
		if (httpClient == null) {
			createAndConfigureHttpClient();
		}
		return httpClient;
	}

	protected void createAndConfigureHttpClient() throws IOException {
		httpClient = createHttpClient();
		configureHttpClient();
	}

	protected void configureHttpClient() throws IOException {
		httpClient.getParams().setAuthenticationPreemptive(isAuthenticationPreemptive());
		initCredentials();
		initSocketFactory();
	}

	protected HttpClient createHttpClient() {
		return new HttpClient();
	}

	protected void initCredentials() {
		if (username != null && password != null) {
			AuthScope authscope = new AuthScope(
					getAuthHost() == null ? AuthScope.ANY_HOST : getAuthHost(),
					AuthScope.ANY_PORT);
			Credentials credentials = new UsernamePasswordCredentials(getUsername(), getPassword());
			httpClient.getState().setCredentials(authscope, credentials);
		}
	}
	
	protected void initSocketFactory() throws IOException {
		if (hostConfig != null) {
			URL trustKeyStoreUrl = null;
			if (hostConfig.getTrustKeyStoreResource() != null) {
				trustKeyStoreUrl = hostConfig.getTrustKeyStoreResource().getURL();
			}
			URL keyStoreUrl = null;
			if (hostConfig.getKeyStoreResource() != null) {
				keyStoreUrl = hostConfig.getKeyStoreResource().getURL();
			}
			ProtocolSocketFactory factory = new AuthSSLProtocolSocketFactory(
					keyStoreUrl, hostConfig.getKeyStorePassword(),
					trustKeyStoreUrl, hostConfig.getTrustKeyStorePassword());
			Protocol protocol = createProtocol(hostConfig, factory);
			httpClient.getHostConfiguration().setHost(hostConfig.getHost(),
					hostConfig.getSecurePort(), protocol);
		}
	}

	private Protocol createProtocol(StoresAndSSLConfig config,
			ProtocolSocketFactory factory) {
		return new Protocol(config.getSecureSchema(), factory, config.getSecurePort());
	}

	public Class<?> getObjectType() {
		return HttpClient.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
