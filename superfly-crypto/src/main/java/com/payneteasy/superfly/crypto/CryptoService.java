package com.payneteasy.superfly.crypto;

import com.payneteasy.superfly.crypto.exception.DecryptException;
import com.payneteasy.superfly.crypto.exception.EncryptException;

public interface CryptoService {
    String encrypt(String strToEncrypt) throws EncryptException;

    String decrypt(String strToDecrypt) throws DecryptException;

    /** True for ciphertexts in the pre-GCM format, which should be re-encrypted. */
    boolean isLegacy(String ciphertext);
}
