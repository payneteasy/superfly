package com.payneteasy.superfly.service.impl.remote.check;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RemoteAuthCryptoServiceImplTest {

    private static final RemoteAuthCryptoServiceImpl SERVICE = new RemoteAuthCryptoServiceImpl();

    private static KeyPairData keys;
    private static PublicKey publicKey;

    @BeforeClass
    public static void generateKeys() throws Exception {
        keys = SERVICE.generateKeyPair(RemoteAuthEncryptionAlgorithm.RSA_OAEP);
        String base64 = keys.publicKey()
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        publicKey = KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(base64)));
    }

    @Test
    public void rsaPkcs1RoundTrip() throws Exception {
        String encrypted = encryptPkcs1("secret-1");

        assertEquals("secret-1", SERVICE.decryptPassword(encrypted, keys.privateKey(), RemoteAuthEncryptionAlgorithm.RSA));
        assertEquals("secret-1", SERVICE.decryptOtp(encrypted, keys.privateKey(), RemoteAuthEncryptionAlgorithm.RSA));
    }

    @Test
    public void rsaOaepSha256RoundTrip() throws Exception {
        String encrypted = encryptOaep("secret-2");

        assertEquals("secret-2", SERVICE.decryptPassword(encrypted, keys.privateKey(), RemoteAuthEncryptionAlgorithm.RSA_OAEP));
        assertEquals("secret-2", SERVICE.decryptOtp(encrypted, keys.privateKey(), RemoteAuthEncryptionAlgorithm.RSA_OAEP));
    }

    @Test
    public void oaepCiphertextIsNotDecryptedAsPkcs1() throws Exception {
        assertDecryptionFails(encryptOaep("secret"), RemoteAuthEncryptionAlgorithm.RSA);
    }

    @Test
    public void pkcs1CiphertextIsNotDecryptedAsOaep() throws Exception {
        assertDecryptionFails(encryptPkcs1("secret"), RemoteAuthEncryptionAlgorithm.RSA_OAEP);
    }

    private void assertDecryptionFails(String encrypted, RemoteAuthEncryptionAlgorithm algorithm) {
        try {
            SERVICE.decryptPassword(encrypted, keys.privateKey(), algorithm);
            fail("must not decrypt with " + algorithm);
        } catch (Exception expected) {
            // expected
        }
    }

    private static String encryptPkcs1(String plain) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return encode(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }

    // As OpenSSL/WebCrypto/Node would: SHA-256 for both the hash and MGF1.
    private static String encryptOaep(String plain) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, new OAEPParameterSpec(
                "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        return encode(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }

    private static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().encodeToString(bytes);
    }
}
