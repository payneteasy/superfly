package com.payneteasy.superfly.hotp;

import java.io.OutputStream;

import com.payneteasy.superfly.spi.HOTPProvider;
import com.payneteasy.superfly.spisupport.HOTPProviderContext;

/**
 * Dummy HOTP provider which does nothing and always authenticates successfully.
 * 
 * Roman Puchkovskiy
 */
public class NullHOTPProvider implements HOTPProvider {
	public void init(HOTPProviderContext context) {
	}
	
	public boolean authenticate(String username, String hotp) {
		return true;
	}

	public String getSequenceForDownloadFileName(String username) {
		throw new UnsupportedOperationException();
	}

	public void outputSequenceForDownload(String username, OutputStream os) {
		throw new UnsupportedOperationException();
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
