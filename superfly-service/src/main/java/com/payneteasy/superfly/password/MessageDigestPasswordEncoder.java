package com.payneteasy.superfly.password;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

/**
 * Uses a {@link MessageDigest} to encode passwords. Please note that algorighm
 * MUST be initailized before using this.
 * 
 * @author Roman Puchkovskiy
 * @see MessageDigest
 */
public class MessageDigestPasswordEncoder extends AbstractPasswordEncoder {
	private String algorithm;

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String encode(String plainPassword, String salt) {
		String saltedPassword = mergePasswordAndSalt(plainPassword, salt);
		MessageDigest md = getMessageDigest();
		byte[] digest = computeDigest(saltedPassword, md);
		return digestToString(digest);
	}

	protected String digestToString(byte[] digest) {
		return new String(Hex.encodeHex(digest));
	}

	protected byte[] computeDigest(String saltedPassword, MessageDigest md) {
		byte[] digest;
		try {
			digest = md.digest(saltedPassword.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
		return digest;
	}

	protected MessageDigest getMessageDigest() {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
		return md;
	}
}
