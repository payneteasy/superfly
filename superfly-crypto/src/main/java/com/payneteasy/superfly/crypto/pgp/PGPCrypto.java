package com.payneteasy.superfly.crypto.pgp;

import java.io.IOException;
import java.io.OutputStream;

import com.payneteasy.superfly.crypto.PublicKeyCrypto;

public class PGPCrypto implements PublicKeyCrypto {

    public boolean isPublicKeyValid(String armoredPublicKey) {
        return PGPUtils.isPublicKeyValid(armoredPublicKey);
    }

    public void encrypt(byte[] clearText, String name, String armoredPublicKey, OutputStream os) throws IOException {
        PGPUtils.encryptBytesAndArmor(clearText, name, armoredPublicKey, os);
    }

}
