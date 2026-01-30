package protocol;

import java.io.IOException;

/**
 * Example: Launch Tor, connect to ControlPort, create a hidden service, and print the onion address.
 */
public class TorIntegrationExample {
    public static void main(String[] args) throws Exception {
        // 1. Launch Tor as an external process (ensure tor is installed and on PATH)
        Process torProcess = new ProcessBuilder("tor", "--ControlPort", "9051").start();
        Thread.sleep(5000); // Wait for Tor to start (in production, check for readiness)

        // 2. Connect to Tor ControlPort
        try (TorControlClient tor = new TorControlClient("127.0.0.1", 9051)) {
            tor.authenticate();
            // 3. Create an ephemeral hidden service (onion address)
            String onion = tor.addOnion(9001, 9001);
            System.out.println("Onion address: " + onion);
        }

        // 4. (Optional) Stop Tor process
        torProcess.destroy();
    }
}
