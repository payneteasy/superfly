package com.payneteasy.superfly.common.codec;


import java.util.Base64;

public class CodecUtils {

    public static String decode(String message) {
        String decoded = new String(Base64.getDecoder().decode(message.getBytes()));
        return decoded;
    }

    public static String encode(String message) {
        String encoded = new String(Base64.getEncoder().encode(message.getBytes()));
        return encoded;
    }
    public static String encode(byte[] message) {
        return new String(Base64.getEncoder().encode(message));
    }

    public static byte[] decode(byte[] bytes) {
        return Base64.getDecoder().decode(bytes);
    }



}
