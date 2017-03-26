package com.payneteasy.superfly.password;

import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        digest = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
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
