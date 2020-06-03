package com.payneteasy.superfly.common.utils;

import com.payneteasy.superfly.api.SsoDecryptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;


/**
 * Created by IntelliJ IDEA.
 * User: vvk
 * Date: 11.06.2007
 * Time: 11:29:45
 * To change this template use File | Settings | File Templates.
 *
 * TODO: remove this class
 */
public class CryptoHelper {
    protected static Logger log = LoggerFactory.getLogger(CryptoHelper.class);

    public final static String ALGORITHM_SHA256 = "SHA-256";


    private static String toHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String SHA256(String input) {
        return toHexString(SHA256byte(input));
    }

    private static byte[] SHA256byte(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_SHA256);
            return md.digest(input.getBytes());
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encrypt(String strToEncrypt, String secret, String salt) throws SsoDecryptException {
        try {
            byte[]          iv     = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory   = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec          spec      = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey        tmp       = factory.generateSecret(spec);
            SecretKeySpec    secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(Charset.defaultCharset())));
        } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new SsoDecryptException(e);
        }
    }

    public static String decrypt(String strToDecrypt, String secret, String salt) throws SsoDecryptException {
        try {
            byte[]          iv     = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory   = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec          spec      = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey        tmp       = factory.generateSecret(spec);
            SecretKeySpec    secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new SsoDecryptException(e);
        }
    }
}
