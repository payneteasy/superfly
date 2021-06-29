package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.spring.Policy;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContext;

/**
 * Overrides context customization to change locations list depending on
 * superfly-policy context parameter.
 *
 * @author Roman Puchkovskiy
 */
public class CustomContextLoaderListener extends ContextLoaderListener {

    @Override
    protected void customizeContext(ServletContext servletContext,
            ConfigurableWebApplicationContext applicationContext) {
        super.customizeContext(servletContext, applicationContext);
        String policy = servletContext.getInitParameter("superfly-policy");
        if (policy == null) {
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
