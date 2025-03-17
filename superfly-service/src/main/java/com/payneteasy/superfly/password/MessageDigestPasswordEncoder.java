package com.payneteasy.superfly.password;

import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.conditional.OnPolicyCondition;
import lombok.Setter;
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;

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
@Setter
@Component
@OnPolicyCondition({Policy.NONE, Policy.PCIDSS})
public class MessageDigestPasswordEncoder extends AbstractPasswordEncoder {
    private String algorithm = "SHA-256";

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
