package com.payneteasy.superfly.crypto.pgp;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.bc.BcPGPObjectFactory;
import org.bouncycastle.openpgp.bc.BcPGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.bc.BcPGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(PGPUtils.class);

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

    public static byte[] decryptBytes(byte[] messageBytes, byte[] keyBytes,
            char[] passwd) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        decryptInputStream(new ByteArrayInputStream(messageBytes),
                new ByteArrayInputStream(keyBytes), passwd, baos);
        return baos.toByteArray();
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

        PGPDataEncryptorBuilder builder = new BcPGPDataEncryptorBuilder(PGPEncryptedData.CAST5)
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

    @SuppressWarnings("rawtypes")
    private static void decryptInputStream(InputStream in, InputStream keyIn,
            char[] passwd, OutputStream os) throws Exception {
        in = PGPUtil.getDecoderStream(in);

        try {
            PGPObjectFactory pgpF = new BcPGPObjectFactory(in);
            PGPEncryptedDataList enc;

            Object o = pgpF.nextObject();
            //
            // the first object might be a PGP marker packet.
            //
            if (o instanceof PGPEncryptedDataList) {
                enc = (PGPEncryptedDataList) o;
            } else {
                enc = (PGPEncryptedDataList) pgpF.nextObject();
            }

            //
            // find the secret key
            //
            Iterator it = enc.getEncryptedDataObjects();
            PGPPrivateKey sKey = null;
            PGPPublicKeyEncryptedData pbe = null;
            PGPSecretKeyRingCollection pgpSec = new BcPGPSecretKeyRingCollection(
                    PGPUtil.getDecoderStream(keyIn));

            while (sKey == null && it.hasNext()) {
                pbe = (PGPPublicKeyEncryptedData) it.next();

                sKey = findSecretKey(pgpSec, pbe.getKeyID(), passwd);
            }

            if (sKey == null) {
                throw new IllegalArgumentException(
                        "secret key for message not found.");
            }

            InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(sKey));

            PGPObjectFactory plainFact = new BcPGPObjectFactory(clear);

            Object message = plainFact.nextObject();

            if (message instanceof PGPCompressedData) {
                PGPCompressedData cData = (PGPCompressedData) message;
                PGPObjectFactory pgpFact = new BcPGPObjectFactory(
                        cData.getDataStream());

                message = pgpFact.nextObject();
            }

            if (message instanceof PGPLiteralData) {
                PGPLiteralData ld = (PGPLiteralData) message;
                OutputStream fOut = os;

                InputStream unc = ld.getInputStream();
                int ch;

                while ((ch = unc.read()) >= 0) {
                    fOut.write(ch);
                }
            } else if (message instanceof PGPOnePassSignatureList) {
                throw new PGPException(
                        "encrypted message contains a signed message - not literal data.");
            } else {
                throw new PGPException(
                        "message is not a simple encrypted file - type unknown.");
            }

            if (pbe.isIntegrityProtected()) {
                if (!pbe.verify()) {
                    System.err.println("message failed integrity check");
                } else {
                    System.err.println("message integrity check passed");
                }
            } else {
                System.err.println("no message integrity check");
            }
        } catch (PGPException e) {
            LOGGER.error("PGPException", e);
            if (e.getUnderlyingException() != null) {
                e.getUnderlyingException().printStackTrace();
            }
        }
    }

    private static PGPPrivateKey findSecretKey(
            PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass)
            throws PGPException, NoSuchProviderException {
        PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

        if (pgpSecKey == null) {
            return null;
        }

        final PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(
                new BcPGPDigestCalculatorProvider()).build(pass);
        return pgpSecKey.extractPrivateKey(decryptor);
    }

    private static void writeBytesToLiteralData(OutputStream out,
            char fileType, String name, byte[] bytes) throws IOException {
        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();
        OutputStream pOut = lData.open(out, fileType, name,
                bytes.length, new Date());
        pOut.write(bytes);
    }
}
