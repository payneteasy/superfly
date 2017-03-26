package com.payneteasy.superfly.crypto;

import java.io.IOException;
import java.io.OutputStream;

public interface PublicKeyCrypto {
    boolean isPublicKeyValid(String armoredPublicKey);
    void encrypt(byte[] clearText, String name, String armoredPublicKey, OutputStream os) throws IOException;
}
