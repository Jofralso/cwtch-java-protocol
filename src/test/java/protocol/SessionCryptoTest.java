package protocol;

import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;
import static org.junit.jupiter.api.Assertions.*;

class SessionCryptoTest {
    @Test
    void testSessionKeyDerivationAndEncryption() throws Exception {
        byte[] sharedSecret = new byte[32];
        byte[] salt = new byte[16];
        byte[] info = "test-session".getBytes();
        for (int i = 0; i < sharedSecret.length; i++) sharedSecret[i] = (byte) i;
        for (int i = 0; i < salt.length; i++) salt[i] = (byte) (i + 1);
        SecretKey key = SessionCrypto.deriveSessionKey(sharedSecret, salt, info);
        assertNotNull(key);
        String plaintext = "Hello, world!";
        String ciphertext = SessionCrypto.encrypt(plaintext, key);
        assertNotNull(ciphertext);
        String decrypted = SessionCrypto.decrypt(ciphertext, key);
        assertEquals(plaintext, decrypted);
    }
}
