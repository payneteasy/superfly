package com.payneteasy.superfly.crypto.pgp;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;
import org.bouncycastle.openpgp.bc.BcPGPObjectFactory;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

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

    @Test
    public void encryptsWithAes256AndIntegrityProtection() throws Exception {
        RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
        generator.init(new RSAKeyGenerationParameters(BigInteger.valueOf(0x10001), new SecureRandom(), 2048, 12));
        PGPKeyPair keyPair = new BcPGPKeyPair(PublicKeyAlgorithmTags.RSA_GENERAL, generator.generateKeyPair(), new Date());

        ByteArrayOutputStream armoredKey = new ByteArrayOutputStream();
        try (ArmoredOutputStream aos = new ArmoredOutputStream(armoredKey)) {
            new PGPPublicKeyRing(List.of(keyPair.getPublicKey())).encode(aos);
        }

        ByteArrayOutputStream encrypted = new ByteArrayOutputStream();
        new PGPCrypto().encrypt("secret message".getBytes(StandardCharsets.UTF_8), "secret.txt",
                armoredKey.toString(StandardCharsets.UTF_8), encrypted);

        BcPGPObjectFactory factory = new BcPGPObjectFactory(
                PGPUtil.getDecoderStream(new ByteArrayInputStream(encrypted.toByteArray())));
        PGPEncryptedDataList list = (PGPEncryptedDataList) factory.nextObject();
        PGPPublicKeyEncryptedData data = (PGPPublicKeyEncryptedData) list.get(0);
        PGPPrivateKey privateKey = keyPair.getPrivateKey();
        BcPublicKeyDataDecryptorFactory decryptor = new BcPublicKeyDataDecryptorFactory(privateKey);

        InputStream clear = data.getDataStream(decryptor);
        Assert.assertEquals(PGPEncryptedData.AES_256, data.getSymmetricAlgorithm(decryptor));

        Object message = new BcPGPObjectFactory(clear).nextObject();
        Assert.assertTrue(message instanceof PGPCompressedData);
        Object literal = new BcPGPObjectFactory(((PGPCompressedData) message).getDataStream()).nextObject();
        Assert.assertTrue(literal instanceof PGPLiteralData);
        ByteArrayOutputStream plain = new ByteArrayOutputStream();
        ((PGPLiteralData) literal).getInputStream().transferTo(plain);

        Assert.assertTrue(data.isIntegrityProtected());
        Assert.assertTrue(data.verify());
        Assert.assertEquals("secret message", plain.toString(StandardCharsets.UTF_8));
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
