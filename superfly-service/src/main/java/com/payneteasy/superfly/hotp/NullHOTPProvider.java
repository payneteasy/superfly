package com.payneteasy.superfly.hotp;

import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.spisupport.ObjectResolver;

/**
 * Dummy HOTP provider which does nothing and always authenticates successfully.
 * 
 * Roman Puchkovskiy
 */
public class NullHOTPProvider implements HOTPProvider {
	public void init(ObjectResolver objectResolver) {
	}
	
	public boolean authenticate(String username, String hotp) {
		return true;
	}
}
