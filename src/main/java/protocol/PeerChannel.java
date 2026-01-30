package protocol;

import java.io.*;
import java.net.Socket;
import javax.crypto.SecretKey;

/**
 * PeerChannel manages a peer-to-peer connection over a socket (e.g., via Tor hidden service),
 * handling sending and receiving ProtocolMessages with encryption.
 */
public class PeerChannel {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final SecretKey sessionKey;
    private long sendCounter = 0;
    private long recvCounter = 0;

    public PeerChannel(Socket socket, SecretKey sessionKey) throws IOException {
        this.socket = socket;
        this.sessionKey = sessionKey;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * Sends a ProtocolMessage (encrypts payload).
     */
    public void send(String type, String plaintext) throws Exception {
        String encrypted = SessionCrypto.encrypt(plaintext, sessionKey);
        ProtocolMessage msg = new ProtocolMessage(type, encrypted, sendCounter++);
        out.println(msg.serialize());
    }

    /**
     * Receives the next ProtocolMessage (decrypts payload).
     */
    public ProtocolMessage receive() throws Exception {
        String line = in.readLine();
        if (line == null) return null;
        ProtocolMessage msg = ProtocolMessage.deserialize(line);
        if (msg.getCounter() != recvCounter++) {
            throw new IOException("Message counter mismatch (possible replay attack)");
        }
        String decrypted = SessionCrypto.decrypt(msg.getPayload(), sessionKey);
        return new ProtocolMessage(msg.getType(), decrypted, msg.getCounter());
    }

    public void close() throws IOException {
        socket.close();
    }
}
