package protocol;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.KeyAgreement;
import java.security.*;
import java.security.spec.NamedParameterSpec;
import java.util.Base64;

/**
 * Handshake handles the ECDH key exchange between two peers to derive a shared session key.
 * This is used for establishing end-to-end encrypted channels in the protocol.
 *
 * Uses X25519 for ECDH and BouncyCastle for cryptography.
 */
public class Handshake {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Generates a new X25519 key pair for ephemeral ECDH.
     */
    public static KeyPair generateEphemeralKeyPair() throws GeneralSecurityException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("X25519", "BC");
        kpg.initialize(new NamedParameterSpec("X25519"));
        return kpg.generateKeyPair();
    }

    /**
     * Computes the shared secret using ECDH (X25519).
     * @param privateKey Our ephemeral private key
     * @param peerPublicKey The peer's ephemeral public key
     * @return The shared secret (32 bytes)
     */
    public static byte[] computeSharedSecret(PrivateKey privateKey, PublicKey peerPublicKey) throws GeneralSecurityException {
        KeyAgreement ka = KeyAgreement.getInstance("X25519", "BC");
        ka.init(privateKey);
        ka.doPhase(peerPublicKey, true);
        return ka.generateSecret();
    }

    /**
     * Encodes a public key as Base64 for transmission.
     */
    public static String encodePublicKey(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Decodes a Base64-encoded public key.
     */
    public static PublicKey decodePublicKey(String base64) throws GeneralSecurityException {
        KeyFactory kf = KeyFactory.getInstance("X25519", "BC");
        return kf.generatePublic(new java.security.spec.XECPublicKeySpec(new NamedParameterSpec("X25519"),
                new java.math.BigInteger(1, Base64.getDecoder().decode(base64))));
    }
}
