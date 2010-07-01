package com.payneteasy.superfly.jira.init;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Context listener which initializes Superfly+Jira machinery on startup.
 * 
 * @author Roman Puchkovskiy
 */
public class InitializerContextListener extends InitializerBase implements ServletContextListener {
	
	public void contextInitialized(ServletContextEvent sce) {
		ServletContext servletContext = sce.getServletContext();
		doInitialize(getPropertiesLocation(servletContext));
	}
	
	public void contextDestroyed(ServletContextEvent sce) {
		initializer.destroy();
	}
	
	protected void doInitialize(String propsLocation) {
		if (propsLocation == null) {
			throw new IllegalStateException("Null properties location");
		}
		properties = loadProperties(propsLocation);
		
		try {
			createInitializer(properties);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	protected String getPropertiesLocation(ServletContext servletContext) {
		return servletContext.getInitParameter("superfly-properties-location");
	}

}
