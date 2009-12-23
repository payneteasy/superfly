package org.payneteasy.superfly.client;

/**
 * Extracts an array of strings from a value.
 * 
 * @author Roman Puchkovskiy
 */
public interface ValuesExtractor {
	/**
	 * Performs extraction.
	 * 
	 * @param object	object from which to extract
	 * @return values
	 */
	String[] extract(Object object);
}
