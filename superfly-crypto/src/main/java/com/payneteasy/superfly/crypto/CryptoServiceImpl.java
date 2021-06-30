package com.payneteasy.superfly.crypto;

import com.payneteasy.superfly.crypto.exception.DecryptException;
import com.payneteasy.superfly.crypto.exception.EncryptException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class CryptoServiceImpl implements CryptoService {
    private String cryptoSecret;
    private String cryptoSalt;

    public void setCryptoSecret(String cryptoSecret) {
        this.cryptoSecret = cryptoSecret;
    }

    public void setCryptoSalt(String cryptoSalt) {
        this.cryptoSalt = cryptoSalt;
    }

    @Override
    public String encrypt(String strToEncrypt) throws EncryptException {
        return encrypt(strToEncrypt, cryptoSecret, cryptoSalt);
    }

    @Override
    public String decrypt(String strToDecrypt) throws DecryptException {
        return decrypt(strToDecrypt, cryptoSecret, cryptoSalt);
    }

    private String encrypt(String strToEncrypt, String secret, String salt) throws EncryptException {
        try {
            byte[]          iv     = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory   = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec      = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp       = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(Charset.defaultCharset())));
        } catch (NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new EncryptException(e);
        }
    }

    private  String decrypt(String strToDecrypt, String secret, String salt) throws DecryptException {
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
            throw new DecryptException(e);
        }
    }
}
