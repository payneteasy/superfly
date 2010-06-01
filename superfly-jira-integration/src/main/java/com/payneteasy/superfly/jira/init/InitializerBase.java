package com.payneteasy.superfly.jira.init;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;

import com.googlecode.xremoting.core.XRemotingProxyFactory;
import com.googlecode.xremoting.core.commonshttpclient.CommonsHttpClientRequester;
import com.googlecode.xremoting.core.commonshttpclient.HttpClientBuilder;
import com.googlecode.xremoting.core.spi.Requester;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.client.CommaDelimitedListActionDescriptionCollector;
import com.payneteasy.superfly.client.SuperflyDataSender;

public class InitializerBase {
	
	private final String CLASSPATH_PREFIX = "classpath:";
	
	protected Properties properties;
	protected Initializer initializer;
	
	protected void createInitializer(Properties properties) throws Exception {
		SSOService ssoService = createSSOService();
		initializer = new Initializer();
		initializer.setSubsystemIdentifier(getProperty("subsystem.name"));
		initializer.setPrincipalName(getProperty("principal.name"));
		initializer.setGroupsCommaDelimitedList(getProperty("actions.list"));
		initializer.setSsoService(ssoService);
		initializer.setSender(createSender(ssoService));
		initializer.init();
	}

	protected SuperflyDataSender createSender(SSOService ssoService) throws Exception {
		CommaDelimitedListActionDescriptionCollector collector = new CommaDelimitedListActionDescriptionCollector();
		collector.setCommaDelimitedList(getProperty("actions.list"));
		SuperflyDataSender sender = new SuperflyDataSender();
		sender.setSsoService(ssoService);
		sender.setSubsystemIdentifier(getProperty("sybsystem.name"));
		sender.setActionDescriptionCollector(collector);
		sender.setAutoSend(false);
		sender.afterPropertiesSet();
		return sender;
	}

	protected Properties loadProperties(String propsLocation) {
		InputStream propsIS = null;
		Properties properties;
		try {
			if (propsLocation.startsWith(CLASSPATH_PREFIX)) {
				propsIS = getInputStreamFromClasspath(propsLocation);
			} else {
				propsIS = getInputStreamFromFile(propsLocation);
			}
			if (propsIS == null) {
				throw new IllegalStateException("Null properties stream: please check whether resource exists: " + propsLocation);
			}
			properties = new Properties();
			properties.load(propsIS);
		} catch (IOException e) {
			throw new IllegalStateException("Could not load properties from the following location: " + propsLocation, e);
		} finally {
			if (propsIS != null) {
				try {
					propsIS.close();
				} catch (IOException e) {
					// ignoring
				}
			}
		}
		return properties;
	}
	
	protected String getProperty(String key) {
		return properties.getProperty(key);
	}

	protected InputStream getInputStreamFromClasspath(String resourceLocation) {
		InputStream propsIS;
		String location = resourceLocation.substring(CLASSPATH_PREFIX.length());
		propsIS = getClass().getClassLoader().getResourceAsStream(location);
		return propsIS;
	}
	
	protected InputStream getInputStreamFromFile(String filename)
			throws FileNotFoundException {
		InputStream propsIS;
		propsIS = new FileInputStream(filename);
		return propsIS;
	}
	
	protected SSOService createSSOService() throws MalformedURLException {
		HttpClient httpClient = createSSOServiceHttpClient();
		
		Requester requester = new CommonsHttpClientRequester(httpClient, getProperty("superfly.sso.service.url"));
		XRemotingProxyFactory proxyFactory = new XRemotingProxyFactory(requester);
		return (SSOService) proxyFactory.create(SSOService.class);
	}

	protected HttpClient createSSOServiceHttpClient()
			throws MalformedURLException {
		String keystoreResource = getProperty("context.keystoreResource");
		String truststoreResource = getProperty("context.truststoreResource");
		URL keystoreUrl = null;
		URL truststoreUrl = null;
		if (keystoreResource != null) {
			keystoreUrl = getResourceUrl(keystoreResource);
			if (keystoreUrl == null) {
				throw new IllegalStateException("Did not find a resource: " + keystoreResource);
			}
		}
		if (truststoreResource != null) {
			truststoreUrl = getResourceUrl(truststoreResource);
			if (truststoreUrl == null) {
				throw new IllegalStateException("Did not find a resource: " + truststoreResource);
			}
		}
		
		HttpClientBuilder builder = HttpClientBuilder.create()
				.ssl(getProperty("superfly.sso.service.host"));
		if (keystoreUrl != null) {
			builder.keyStore(keystoreUrl, getProperty("context.keystorePassword"));
		}
		if (truststoreUrl != null) {
			builder.trustKeyStore(truststoreUrl, getProperty("context.truststorePassword"));
		}
		builder.securePort(Integer.parseInt(getProperty("superfly.sso.service.secure.port")));
		HttpClient httpClient = builder.build();
		return httpClient;
	}
	
	protected URL getResourceUrl(String location) throws MalformedURLException {
		if (location.startsWith(CLASSPATH_PREFIX)) {
			String loc = location.substring(CLASSPATH_PREFIX.length());
			return getClasspathResourceUrl(loc);
		} else {
			return getFileResourceUrl(location);
		}
	}

	protected URL getClasspathResourceUrl(String loc) {
		return getClass().getClassLoader().getResource(loc);
	}
	
	protected URL getFileResourceUrl(String location) throws MalformedURLException {
		return new File(location).toURI().toURL();
	}

}
