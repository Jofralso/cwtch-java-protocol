package protocol;

/**
 * Example: Launch Tor, connect to ControlPort, create a hidden service, and print the onion address.
 */
public class TorIntegrationExample {
    public static void main(String[] args) throws Exception {
        // If Tor is already running as a service, skip launching it here.
        // Otherwise, you can launch Tor as a process as below (commented out):
        // Process torProcess = new ProcessBuilder("tor", "--ControlPort", "9051").start();
        // Thread.sleep(5000); // Wait for Tor to start (in production, check for readiness)

        // Connect to Tor ControlPort and authenticate automatically (using cookie if available)
        try (TorControlClient tor = new TorControlClient("127.0.0.1", 9051)) {
            tor.authenticateWithCookie(null); // null = auto-detect cookie file
            // Create an ephemeral hidden service (onion address)
            String onion = tor.addOnion(9001, 9001);
            System.out.println("Onion address: " + onion);
        }

        // If you launched Tor above, you can stop it here:
        // torProcess.destroy();
    }
}
