package com.payneteasy.superfly.crypto.pgp;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PGPUtilsTest {
    @Test
	public void testEncrypt() throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String armoredPublicKey = readArmoredPublicKey("public_key");
		
		PGPUtils.encryptBytesAndArmor("secret message".getBytes(), "secret.txt", armoredPublicKey, baos);
		baos.close();
		String encryptedMessage = baos.toString();
		System.out.println(encryptedMessage);
	}

    @Test
	public void testInvalidPublicKey() throws Exception {
		try {
			PGPUtils.encryptBytesAndArmor("secret message".getBytes(), "secret.txt", readArmoredPublicKey("invalid_public_key1"), new ByteArrayOutputStream());
            Assert.fail();
		} catch (EOFException e) {
			// expected
		}
		
		try {
			PGPUtils.encryptBytesAndArmor("secret message".getBytes(), "secret.txt", readArmoredPublicKey("invalid_public_key2"), new ByteArrayOutputStream());
            Assert.fail();
		} catch (Exception e) {
			// expected
		}
	}

    @Test
	public void testIsPublicKeyValid() {
        Assert.assertFalse(PGPUtils.isPublicKeyValid("lalala, i'm not a key!"));
	}

	private String readArmoredPublicKey(String fileName) throws IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
		Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
		
		StringBuilder buf = new StringBuilder();
		while (true) {
			int c = reader.read();
			if (c < 0) {
				break;
			} else {
				buf.append((char) c);
			}
		}
		is.close();
        return buf.toString();
	}
}
