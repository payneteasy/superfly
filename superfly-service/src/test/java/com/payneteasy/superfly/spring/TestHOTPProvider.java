package com.payneteasy.superfly.spring;

import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.spisupport.HOTPProviderContext;

public class TestHOTPProvider implements HOTPProvider {
	
	private boolean initialized = false;
	
	public void init(HOTPProviderContext context) {
		initialized = true;
	}

	public boolean authenticate(String username, String hotp) {
		return false;
	}

	public boolean isInitialized() {
		return initialized;
	}

}
