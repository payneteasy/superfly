package com.payneteasy.superfly.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


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
}
