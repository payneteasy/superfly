package com.payneteasy.superfly.web.spring;

import javax.servlet.ServletContext;

import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;

import com.payneteasy.superfly.spring.Policy;

/**
 * Overrides context customization to change locations list depending on
 * superfly-policy context parameter.
 *
 * @author Roman Puchkovskiy
 */
public class CustomContextLoader extends ContextLoader {

	@Override
	protected void customizeContext(ServletContext servletContext,
			ConfigurableWebApplicationContext applicationContext) {
		super.customizeContext(servletContext, applicationContext);
		String policy = servletContext.getInitParameter("superfly-policy");
		if (policy == null) {
			policy = Policy.NONE.getIdentifier();
		}
		String disableHotp = servletContext.getInitParameter("disable-hotp");
		if ("true".equals(disableHotp)) {
			policy = Policy.NONE.getIdentifier();
		}
		String[] oldLocations = applicationContext.getConfigLocations();
		String[] newLocations = new String[oldLocations.length];
		for (int i = 0; i < oldLocations.length; i++) {
			newLocations[i] = oldLocations[i].replaceAll("\\!policy\\!", policy);
		}
		applicationContext.setConfigLocations(newLocations);
	}

}
