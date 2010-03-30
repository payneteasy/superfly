package com.payneteasy.superfly.security;

/**
 * Prepends a prefix to a transformed string.
 * 
 * @author Roman Puchkovskiy
 */
public class PrependingTransformer implements StringTransformer {
	
	private String prefix = "";

	/**
	 * Sets a prefix to prepend.
	 * 
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String transform(String s) {
		return prefix + s;
	}

}
