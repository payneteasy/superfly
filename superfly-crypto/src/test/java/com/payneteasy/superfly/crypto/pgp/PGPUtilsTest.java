package com.payneteasy.superfly.crypto.pgp;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import com.payneteasy.superfly.crypto.pgp.PGPUtils;

import junit.framework.TestCase;

public class PGPUtilsTest extends TestCase {
	public void testEncrypt() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String armoredPublicKey = readArmoredPublicKey("public_key");
		
		PGPUtils.encryptBytesAndArmor("secret message".getBytes(), "secret.txt", armoredPublicKey, baos);
		baos.close();
		String encryptedMessage = baos.toString();
		System.out.println(encryptedMessage);
	}
	
	public void testInvalidPublicKey() throws Exception {
		try {
			PGPUtils.encryptBytesAndArmor("secret message".getBytes(), "secret.txt", readArmoredPublicKey("invalid_public_key1"), new ByteArrayOutputStream());
			fail();
		} catch (EOFException e) {
			// expected
		}
		
		try {
			PGPUtils.encryptBytesAndArmor("secret message".getBytes(), "secret.txt", readArmoredPublicKey("invalid_public_key2"), new ByteArrayOutputStream());
			fail();
		} catch (Exception e) {
			// expected
		}
	}

	private String readArmoredPublicKey(String fileName) throws UnsupportedEncodingException,
			IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
		Reader reader = new InputStreamReader(is, "utf-8");
		
		StringBuffer buf = new StringBuffer();
		while (true) {
			int c = reader.read();
			if (c < 0) {
				break;
			} else {
				buf.append((char) c);
			}
		}
		is.close();
		String armoredPublicKey = buf.toString();
		return armoredPublicKey;
	}
}
