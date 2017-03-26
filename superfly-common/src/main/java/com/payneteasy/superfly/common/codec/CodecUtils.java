package com.payneteasy.superfly.common.codec;

import org.apache.commons.codec.binary.Base64;


public class CodecUtils {

    public static String decode(String message) {
        Base64 codec = initializeCodec();
        String decoded = new String(codec.decode(message.getBytes()));
        return decoded;
    }

    public static String encode(String message) {
        Base64 codec = initializeCodec();
        String encoded = new String(codec.decode(message.getBytes()));
        return encoded;
    }

    private static Base64 initializeCodec() {
        Base64 codec = new Base64();
        return codec;
    }

    public static String encode(byte[] message) {
        Base64 codec = initializeCodec();
        return new String(codec.encode(message));
    }

    public static byte[] decode(byte[] bytes) {
        Base64 codec = initializeCodec();
        return codec.decode(bytes);
    }



}
