package com.payneteasy.superfly.hotp;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.payneteasy.superfly.spi.HOTPProvider;

/**
 * Utilities to deal with {@link HOTPProvider}s.
 *
 * @author Roman Puchkovskiy
 */
public class HOTPProviderUtils {
	/**
	 * Instantiates a provider using service loader.
	 * 
	 * @param allowTestProvider		whether test provider is allowed (must be
	 * false for normal cases)
	 * @return instantiated provider or null
	 */
	public static HOTPProvider instantiateProvider(boolean allowTestProvider) {
		HOTPProvider resultProvider = null;
		ServiceLoader<HOTPProvider> loader = ServiceLoader.load(HOTPProvider.class);
		Iterator<HOTPProvider> iterator = loader.iterator();
		if (iterator.hasNext()) {
			while (iterator.hasNext()) {
				HOTPProvider provider = iterator.next();
				if (!"com.payneteasy.superfly.spring.TestHOTPProvider".equals(provider.getClass().getName())
						|| allowTestProvider) {
					resultProvider = provider;
					break;
				}
			}
		}
		return resultProvider;
	}

}
