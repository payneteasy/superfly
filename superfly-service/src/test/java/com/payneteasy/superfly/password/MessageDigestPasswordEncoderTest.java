package com.payneteasy.superfly.password;

import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.common.utils.CryptoHelper;
import com.payneteasy.superfly.utils.RandomGUID;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

public class MessageDigestPasswordEncoderTest {

    @Test
    public void testMd5() {
        MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
        encoder.setAlgorithm("MD5");
        String encoded = encoder.encode("password", "salt");
        String expected = DigestUtils.md5Hex("password{salt}");
        assertEquals(expected, encoded);
    }

    @Test
    public void testSha1() {
        MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
        encoder.setAlgorithm("SHA1");
        String encoded = encoder.encode("password", "salt");
        String expected = DigestUtils.sha1Hex("password{salt}");
        assertEquals(expected, encoded);
    }

    @Test
    public void testSha256() throws NoSuchAlgorithmException {
        MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
        encoder.setAlgorithm("SHA-256");
        String encoded = encoder.encode("password", "salt");
        MessageDigest md = MessageDigest.getInstance("sha-256");
        byte[] bytes = md.digest("password{salt}".getBytes(StandardCharsets.UTF_8));
        String expected = new String(Hex.encodeHex(bytes));
        assertEquals(expected, encoded);
    }

    @Test
    public void testNewPassword() {
        RandomGUID guid=new RandomGUID(true);
        String salt= CryptoHelper.SHA256(guid.toString());
        System.out.println("salt");
        System.out.println(salt);
        MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();
        encoder.setAlgorithm("SHA-256");
        String password = encoder.encode("_123Admin123", salt);
        System.out.println("password");
        System.out.println(password);

        System.out.println(encoder.encode("_123Admin123", "3caffd7f8d4519cdd110ce3089431e7214635f4ff3f9235a94e3227e9b831e0f"));
    }

    @Test
    public void testSuccessWorkOfEncrypting() {
        String text = "some text to encrypt";
        String secret = "secret";
        String salt = "salt";
        try {
            String encrypt = CryptoHelper.encrypt(text, secret, salt);
            System.out.println(encrypt);
            String decrypt = CryptoHelper.decrypt(encrypt, secret, salt);
            assertEquals(text, decrypt);
        } catch (SsoDecryptException e) {
            e.printStackTrace();
        }
    }

    @Test(expected = SsoDecryptException.class)
    public void testWrongSaltWorkOfEncrypting() throws SsoDecryptException {
        String text = "some text to encrypt";
        String secret = "secret";
        String salt = "salt";

        String encrypt = CryptoHelper.encrypt(text, secret, salt);
        System.out.println(encrypt);
        CryptoHelper.decrypt(encrypt, secret, salt + "ups");
    }

    @Test(expected = SsoDecryptException.class)
    public void testWrongSecretWorkOfEncrypting() throws SsoDecryptException {
        String text = "some text to encrypt";
        String secret = "secret";
        String salt = "salt";

        String encrypt = CryptoHelper.encrypt(text, secret, salt);
        System.out.println(encrypt);
        CryptoHelper.decrypt(encrypt, secret + "ups", salt);
    }
}
