package protocol;

/**
 * ProtocolMessage defines the structure for all messages exchanged between peers.
 * All fields except the encrypted payload are minimal to reduce metadata exposure.
 */
public class ProtocolMessage {
    // Message type: e.g., "handshake", "chat", "group", etc.
    private final String type;
    // Encrypted payload (Base64-encoded)
    private final String payload;
    // Optional: message counter for replay protection
    private final long counter;

    public ProtocolMessage(String type, String payload, long counter) {
        this.type = type;
        this.payload = payload;
        this.counter = counter;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }

    public long getCounter() {
        return counter;
    }

    /**
     * Serializes the message to a compact string (for transmission).
     */
    public String serialize() {
        return type + "," + counter + "," + payload;
    }

    /**
     * Parses a message from a serialized string.
     */
    public static ProtocolMessage deserialize(String data) {
        String[] parts = data.split(",", 3);
        if (parts.length != 3) throw new IllegalArgumentException("Invalid message format");
        return new ProtocolMessage(parts[0], parts[2], Long.parseLong(parts[1]));
    }
}
