package com.payneteasy.superfly.common.utils;

import com.payneteasy.superfly.common.codec.CodecUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;


/**
 * Created by IntelliJ IDEA.
 * User: vvk
 * Date: 11.06.2007
 * Time: 11:29:45
 * To change this template use File | Settings | File Templates.
 */
public class CryptoHelper {
    protected static Logger log = LoggerFactory.getLogger(CryptoHelper.class);

    public final static String ALGORITHM_MD5 = "MD5";
    public final static String ALGORITHM_SHA1 = "SHA-1";
    public final static String ALGORITHM_SHA256 = "SHA-256";
    private final static String SECRET_KEY = "ursus marsicanus";
    private final static String ALGORITHM_AES = "AES/CBC/NoPadding";
    private final static String ALGORITHM_DES = "DESede";
    private final static String ALGORITHM_BLOWFISH = "Blowfish/CBC/PKCS5Padding";
    private final static String ALGORITHM_BLOWFISH_KEY = "Blowfish";
    private final static String SECRET_KEY_SOFTMARY = MD5("SOFTMARY");
    private final static String HMAC_SHA1_ALGORITHM = "HmacSHA1";


    public static String Crypt(String data) {
        Cipher cipher;
        byte[] outText = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM_BLOWFISH);

            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM_BLOWFISH_KEY);

            IvParameterSpec ivSpec = new IvParameterSpec(MD5(SECRET_KEY).substring(0, 8).getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            outText = cipher.doFinal(data.getBytes("UTF8"));

        } catch (NoSuchAlgorithmException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        } catch (BadPaddingException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        } catch (InvalidAlgorithmParameterException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        }

        return CodecUtils.encode(outText);
    }

    public static String Decrypt(String data) {
        Cipher cipher;
        String result = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM_BLOWFISH);

            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM_BLOWFISH_KEY);
            IvParameterSpec ivSpec = new IvParameterSpec(MD5(SECRET_KEY).substring(0, 8).getBytes());


            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] inputBytes = CodecUtils.decode(data.getBytes("utf-8"));
            byte[] outputBytes = cipher.doFinal(inputBytes);

            result = new String(outputBytes, "UTF8");


        } catch (NoSuchAlgorithmException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        } catch (BadPaddingException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        } catch (InvalidAlgorithmParameterException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        } catch (IllegalBlockSizeException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        } catch (InvalidKeyException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        } catch (IOException e) {
            if (log.isErrorEnabled()) log.error(e.getMessage(), e);
        }

        return result;
    }

    public static String MD5(String input) {
        return MD5(input.getBytes());
    }

    public static String MD5(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_MD5);

            byte[] messageDigest = md.digest(input);

            return toHexString(messageDigest);

        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    private static String toHexString(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (byte b : data) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) sb.append('0');
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String SHA1(String input) {
        return toHexString(SHA1byte(input));
    }

    public static byte[] SHA1byte(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_SHA1);
            return md.digest(input.getBytes());
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public static String SHA256(String input) {
        return toHexString(SHA256byte(input));
    }

    public static byte[] SHA256byte(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_SHA256);
            return md.digest(input.getBytes());
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public static String CommonPackHex(String str) {
        StringBuilder res = new StringBuilder();
        char[] s2 = SECRET_KEY_SOFTMARY.toCharArray();
        int i = 0;
        for (char v : str.toCharArray()) {
            res.append(String.format("%02x", v ^ s2[i]));
            if (++i == s2.length) i = 0;
        }
        return res.toString();
    }


    public static String encodeUrlParam(long param) {
        return encodeUrlParam(String.valueOf(param));
    }

    public static String encodeUrlParam(String aPlainText) {
        String base64Text = Crypt(aPlainText);
        if (base64Text == null) {
            return null;
        } else {
            Hex hex = new Hex();
            try {
                String hexText = new String(hex.encode(base64Text.getBytes("UTF-8")), "UTF-8");
                return "bb" + hexText;
            } catch (Exception e) {
                log.error("Error encode " + base64Text + " to hex", e);
                return null;
            }
        }

    }

    public static String decodeUrlParam(String aHexText) {
        Hex hex = new Hex();

        if (aHexText != null && aHexText.startsWith("bb")) {
            try {
                String base64Text = new String(hex.decode(aHexText.substring(2).getBytes("UTF-8")), "UTF-8");
                return Decrypt(base64Text);

            } catch (Exception e) {
                log.error("Error decode hex " + aHexText + " to base 64", e);
                return null;
            }
        } else {
            return null;
        }
    }

    protected static String base64Encode(String data) {
        return CodecUtils.encode(data);
    }

    protected static String base64Decode(String data) {
        return CodecUtils.decode(data);
    }

    public static String HMAC_SHA1(String data, String key) {
        String result = "";
        try {
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(decodeHexString(key), HMAC_SHA1_ALGORITHM);

            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            // compute the hmac on input data bytes
            result = toHexString(mac.doFinal(data.getBytes()));

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }


    public static byte[] decodeHexString(final String encoded) {
        byte result[] = new byte[encoded.length() / 2];
        char enc[] = encoded.toUpperCase().toCharArray();
        StringBuffer curr;
        int k = 0;
        for (int i = 0; i < enc.length; i += 2) {
            curr = new StringBuffer("");
            curr.append(String.valueOf(enc[i]));
            curr.append(String.valueOf(enc[i + 1]));
            result[k++] = (byte) Integer.parseInt(curr.toString(), 16);
        }
        return result;
    }

}
