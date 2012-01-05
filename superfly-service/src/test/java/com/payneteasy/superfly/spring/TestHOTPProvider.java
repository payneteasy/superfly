package com.payneteasy.superfly.spring;

import java.io.OutputStream;

import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.spisupport.HOTPProviderContext;

public class TestHOTPProvider implements HOTPProvider {
	
	private boolean initialized = false;
	
	public void init(HOTPProviderContext context) {
		initialized = true;
	}

	public boolean authenticate(String subsystemIdentifier, String username, String hotp) {
		return false;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public String getSequenceForDownloadFileName(String username) {
		return null;
	}

	public void outputSequenceForDownload(String username, OutputStream os) {
	}

	public boolean outputsSequenceForDownload() {
		return false;
	}

	public String computeValue(String username, long counter) {
		return "111111";
	}

	public void resetSequence(String username) {
	}

}
