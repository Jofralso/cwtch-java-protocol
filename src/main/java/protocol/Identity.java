package protocol;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;
import java.util.Base64;

/**
 * Identity represents a user's long-term cryptographic identity (Ed25519/X25519 key pair)
 * for the Cwtch-style protocol. It is used for authentication, session establishment,
 * and as the basis for onion address generation.
 *
 * This class provides methods for key generation, serialization, and loading from storage.
 */
public class Identity {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Generates a new Ed25519 key pair for identity.
     */
    public static Identity generate() throws GeneralSecurityException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("Ed25519", "BC");
        KeyPair kp = kpg.generateKeyPair();
        return new Identity(kp.getPrivate(), kp.getPublic());
    }

    public Identity(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Returns the public key as a Base64-encoded string (for sharing).
     */
    public String getPublicKeyBase64() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Returns the private key as a Base64-encoded string (for storage).
     */
    public String getPrivateKeyBase64() {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    // TODO: Add methods for loading/saving keys from/to disk securely.
}
