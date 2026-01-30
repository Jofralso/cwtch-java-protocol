package protocol;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProtocolMessageTest {
    @Test
    void testSerializationAndDeserialization() {
        ProtocolMessage msg = new ProtocolMessage("chat", "encryptedPayload", 42);
        String serialized = msg.serialize();
        ProtocolMessage parsed = ProtocolMessage.deserialize(serialized);
        assertEquals(msg.getType(), parsed.getType());
        assertEquals(msg.getPayload(), parsed.getPayload());
        assertEquals(msg.getCounter(), parsed.getCounter());
    }
}
