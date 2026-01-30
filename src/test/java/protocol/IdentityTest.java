package protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.security.KeyPair;
import java.security.GeneralSecurityException;

class IdentityTest {
    @Test
    void testKeyGenerationAndSerialization() throws GeneralSecurityException {
        Identity id = Identity.generate();
        assertNotNull(id.getPrivateKey());
        assertNotNull(id.getPublicKey());
        assertNotNull(id.getPublicKeyBase64());
        assertNotNull(id.getPrivateKeyBase64());
        assertTrue(id.getPublicKeyBase64().length() > 0);
        assertTrue(id.getPrivateKeyBase64().length() > 0);
    }
}
