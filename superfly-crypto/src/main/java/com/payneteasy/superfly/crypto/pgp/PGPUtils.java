package com.payneteasy.superfly.crypto.pgp;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;

public class PGPUtils {
    public static void encryptBytesAndArmor(byte[] clearText, String name,
            String armoredPublicKey, OutputStream os) throws IOException {
        try {
            byte[] publicKeyBytes = armoredToBytes(armoredPublicKey);
            PGPPublicKey publicKey = readPublicKey(publicKeyBytes);
            encryptBytes(os, name, clearText, publicKey, true, true);
        } catch (NoSuchProviderException e) {
            throw new IllegalStateException(e);
        } catch (PGPException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean isPublicKeyValid(String armoredPublicKey) {
        boolean ok;
        try {
            PGPPublicKey key = readPublicKey(armoredToBytes(armoredPublicKey));
            ok = key != null && key.isEncryptionKey();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        } catch (IOException | PGPException e) {
            ok = false;
        } catch (IllegalArgumentException e) {
            // this happens if some junk is supplied as a key
            ok = false;
        }
        return ok;
    }

    private static byte[] armoredToBytes(String armoredPublicKey)
            throws UnsupportedEncodingException {
        return armoredPublicKey.getBytes(StandardCharsets.UTF_8);
    }

    private static PGPPublicKey readPublicKey(byte[] bytes)
            throws IOException, PGPException {
        return readPublicKey(new ByteArrayInputStream(bytes));
    }

    @SuppressWarnings("rawtypes")
    private static PGPPublicKey readPublicKey(InputStream in)
            throws IOException, PGPException {
        in = PGPUtil.getDecoderStream(in);

        PGPPublicKeyRingCollection pgpPub = new BcPGPPublicKeyRingCollection(in);

        //
        // we just loop through the collection till we find a key suitable for
        // encryption, in the real
        // world you would probably want to be a bit smarter about this.
        //

        //
        // iterate through the key rings.
        //
        Iterator rIt = pgpPub.getKeyRings();

        while (rIt.hasNext()) {
            PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
            Iterator kIt = kRing.getPublicKeys();

            while (kIt.hasNext()) {
                PGPPublicKey k = (PGPPublicKey) kIt.next();

                if (k.isEncryptionKey()) {
                    return k;
                }
            }
        }

        throw new IllegalArgumentException(
                "Can't find encryption key in key ring.");
    }

    private static void encryptBytes(OutputStream out, String name, byte[] bytes,
            PGPPublicKey encKey, boolean armor, boolean withIntegrityCheck)
            throws IOException, NoSuchProviderException, PGPException {
        if (armor) {
            out = new ArmoredOutputStream(out);
        }

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(
                PGPCompressedData.ZIP);

        try {
            writeBytesToLiteralData(comData.open(bOut),
                    PGPLiteralData.BINARY, name, bytes);
        } finally {
            comData.close();
        }

        PGPDataEncryptorBuilder builder = new BcPGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                .setWithIntegrityPacket(withIntegrityCheck)
                .setSecureRandom(new SecureRandom())
                ;
        PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(builder);

        cPk.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(encKey));

        byte[] outBytes = bOut.toByteArray();

        try (OutputStream cOut = cPk.open(out, outBytes.length)) {
            cOut.write(outBytes);
        }

        if (armor) {
            // Closing as this leads to writing the required format delimiter.
            // This does not close an underlying stream.
            out.close();
        }
    }

    private static void writeBytesToLiteralData(OutputStream out,
            char fileType, String name, byte[] bytes) throws IOException {
        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
        OutputStream pOut = lData.open(out, fileType, name,
                bytes.length, new Date());
        pOut.write(bytes);
    }
}
