package com.payneteasy.superfly.password;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.payneteasy.superfly.common.utils.CryptoHelper;
import com.payneteasy.superfly.utils.RandomGUID;
import junit.framework.TestCase;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public class MessageDigestPasswordEncoderTest extends TestCase {
	
	public void testMd5() {
		MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
		encoder.setAlgorithm("MD5");
		String encoded = encoder.encode("password", "salt");
		String expected = DigestUtils.md5Hex("password{salt}");
		assertEquals(expected, encoded);
	}
	
	public void testSha1() {
		MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
		encoder.setAlgorithm("SHA1");
		String encoded = encoder.encode("password", "salt");
		String expected = DigestUtils.shaHex("password{salt}");
		assertEquals(expected, encoded);
	}
	
	public void testSha256() throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
		encoder.setAlgorithm("SHA-256");
		String encoded = encoder.encode("password", "salt");
		MessageDigest md = MessageDigest.getInstance("sha-256");
		byte[] bytes = md.digest("password{salt}".getBytes("utf-8"));
		String expected = new String(Hex.encodeHex(bytes));
		assertEquals(expected, encoded);
	}

    public void testNewPassword() {
        RandomGUID guid=new RandomGUID(true);
        String salt= CryptoHelper.SHA256(guid.toString());
        System.out.println(salt);
        MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
        encoder.setAlgorithm("SHA-256");
        String password = encoder.encode("123admin123", salt);
        System.out.println(password);
    }

}
