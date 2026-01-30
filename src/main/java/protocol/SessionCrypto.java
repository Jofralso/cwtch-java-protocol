package protocol;

import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

/**
 * SessionCrypto handles session key derivation (HKDF) and message encryption/decryption (AES-GCM)
 * for the Cwtch-style protocol.
 */
public class SessionCrypto {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final int AES_KEY_SIZE = 32; // 256 bits
    private static final int GCM_IV_SIZE = 12; // 96 bits
    private static final int GCM_TAG_SIZE = 128; // bits

    /**
     * Derives a 256-bit AES key from the shared secret using HKDF.
     */
    public static SecretKey deriveSessionKey(byte[] sharedSecret, byte[] salt, byte[] info) {
        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new org.bouncycastle.crypto.digests.SHA256Digest());
        hkdf.init(new HKDFParameters(sharedSecret, salt, info));
        byte[] key = new byte[AES_KEY_SIZE];
        hkdf.generateBytes(key, 0, AES_KEY_SIZE);
        return new SecretKeySpec(key, "AES");
    }

    /**
     * Encrypts a message using AES-GCM.
     */
    public static String encrypt(String plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[GCM_IV_SIZE];
        new SecureRandom().nextBytes(iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_SIZE, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
        byte[] result = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
        return Base64.getEncoder().encodeToString(result);
    }

    /**
     * Decrypts a message using AES-GCM.
     */
    public static String decrypt(String ciphertextBase64, SecretKey key) throws Exception {
        byte[] data = Base64.getDecoder().decode(ciphertextBase64);
        byte[] iv = new byte[GCM_IV_SIZE];
        System.arraycopy(data, 0, iv, 0, GCM_IV_SIZE);
        byte[] ciphertext = new byte[data.length - GCM_IV_SIZE];
        System.arraycopy(data, GCM_IV_SIZE, ciphertext, 0, ciphertext.length);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext);
    }
}
