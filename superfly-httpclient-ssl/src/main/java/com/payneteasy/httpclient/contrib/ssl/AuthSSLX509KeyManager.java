package com.payneteasy.httpclient.contrib.ssl;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509KeyManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AuthSSLX509KeyManager implements X509KeyManager {
	
    private X509KeyManager defaultKeyManager = null;

    /** Log object for this class. */
    private static final Log LOG = LogFactory.getLog(AuthSSLX509KeyManager.class);

	public AuthSSLX509KeyManager(X509KeyManager keyManager) {
		super();
		defaultKeyManager = keyManager;
	}

	public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
		String result = defaultKeyManager.chooseClientAlias(keyType, issuers, socket);
		return result;
	}

	public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
		String result = defaultKeyManager.chooseServerAlias(keyType, issuers, socket);
		return result;
	}

	public X509Certificate[] getCertificateChain(String alias) {
		X509Certificate[] result = defaultKeyManager.getCertificateChain(alias);
		return result;
	}

	public String[] getClientAliases(String keyType, Principal[] issuers) {
		String[] result = defaultKeyManager.getClientAliases(keyType, issuers);
		return result;
	}

	public PrivateKey getPrivateKey(String alias) {
		PrivateKey result = defaultKeyManager.getPrivateKey(alias);
		return result;
	}

	public String[] getServerAliases(String keyType, Principal[] issuers) {
		String[] result = defaultKeyManager.getServerAliases(keyType, issuers);
		return result;
	}

}
