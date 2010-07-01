package com.payneteasy.superfly.jira.init;

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
import com.payneteasy.superfly.client.utils.CommonUtils;

/**
 * Base class which contains logic for initialization of Superfly+Jira
 * integration machinery. It's meant to be subclassed by (for instance)
 * context listeners or some other classes which instances have their own
 * lifecycle.
 * 
 * @author Roman Puchkovskiy
 */
public abstract class InitializerBase {
	
	protected Properties properties;
	protected Initializer initializer;
	
	protected void createInitializer(Properties properties) throws Exception {
		SSOService ssoService = createSSOService();
		initializer = new Initializer();
		initializer.setSubsystemIdentifier(getProperty("subsystem.name"));
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
		return CommonUtils.loadPropertiesThrowing(propsLocation);
	}
	
	protected String getProperty(String key) {
		return properties.getProperty(key);
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
			keystoreUrl = CommonUtils.getResourceUrl(keystoreResource);
			if (keystoreUrl == null) {
				throw new IllegalStateException("Did not find a resource: " + keystoreResource);
			}
		}
		if (truststoreResource != null) {
			truststoreUrl = CommonUtils.getResourceUrl(truststoreResource);
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
	
}
