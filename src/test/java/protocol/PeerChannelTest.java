package protocol;

import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import static org.junit.jupiter.api.Assertions.*;

class PeerChannelTest {
    @Test
    void testSendAndReceive() throws Exception {
        // Use a loopback socket pair
        ServerSocket server = new ServerSocket(0);
        int port = server.getLocalPort();
        Thread clientThread = new Thread(() -> {
            try {
                Socket clientSocket = new Socket("127.0.0.1", port);
                SecretKey key = SessionCrypto.deriveSessionKey(new byte[32], new byte[16], "test".getBytes());
                PeerChannel channel = new PeerChannel(clientSocket, key);
                channel.send("chat", "Hello from client!");
                channel.close();
            } catch (Exception ignored) {}
        });
        clientThread.start();
        Socket serverSocket = server.accept();
        SecretKey key = SessionCrypto.deriveSessionKey(new byte[32], new byte[16], "test".getBytes());
        PeerChannel channel = new PeerChannel(serverSocket, key);
        ProtocolMessage msg = channel.receive();
        assertEquals("chat", msg.getType());
        assertEquals("Hello from client!", msg.getPayload());
        channel.close();
        server.close();
    }
}
