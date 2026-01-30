package protocol;

import org.junit.jupiter.api.Test;
import java.security.KeyPair;
import static org.junit.jupiter.api.Assertions.*;

class HandshakeTest {
    @Test
    void testEphemeralKeyPairAndSharedSecret() throws Exception {
        KeyPair alice = Handshake.generateEphemeralKeyPair();
        KeyPair bob = Handshake.generateEphemeralKeyPair();
        byte[] aliceSecret = Handshake.computeSharedSecret(alice.getPrivate(), bob.getPublic());
        byte[] bobSecret = Handshake.computeSharedSecret(bob.getPrivate(), alice.getPublic());
        assertArrayEquals(aliceSecret, bobSecret);
        assertEquals(32, aliceSecret.length);
    }
}
