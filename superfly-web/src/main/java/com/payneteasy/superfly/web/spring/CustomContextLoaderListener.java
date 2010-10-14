package com.payneteasy.superfly.web.spring;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;

/**
 * Overrides context customization to change locations list depending on
 * superfly-policy context parameter.
 *
 * @author Roman Puchkovskiy
 */
public class CustomContextLoaderListener extends ContextLoaderListener {

	@Override
	protected ContextLoader createContextLoader() {
		return new CustomContextLoader();
	}

}
