package protocol;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * TorManager handles launching a Tor process, creating a hidden service,
 * and retrieving the onion address for the client.
 *
 * This class assumes Tor is installed and available on the system PATH.
 * For production, bundle Tor or use a Java Tor library.
 */
public class TorManager {
    private Process torProcess;
    private String onionAddress;
    private int hiddenServicePort;

    /**
     * Starts Tor and creates a hidden service on the given port.
     * Returns the onion address (hostname) for this client.
     */
    public String startHiddenService(int localPort) throws IOException, InterruptedException {
        // For demo: use a temporary torrc file and DataDirectory
        String dataDir = "tor_data_" + System.currentTimeMillis();
        String torrc = dataDir + "/torrc";
        java.nio.file.Files.createDirectories(java.nio.file.Paths.get(dataDir));
        java.nio.file.Files.write(java.nio.file.Paths.get(torrc), (
                "HiddenServiceDir " + dataDir + "/hs\n" +
                "HiddenServicePort 9001 127.0.0.1:" + localPort + "\n"
        ).getBytes());

        ProcessBuilder pb = new ProcessBuilder(
                "tor",
                "-f", torrc,
                "--DataDirectory", dataDir
        );
        pb.redirectErrorStream(true);
        torProcess = pb.start();

        // Wait for the hidden service hostname file to appear
        java.nio.file.Path hsHostname = java.nio.file.Paths.get(dataDir, "hs", "hostname");
        for (int i = 0; i < 60; i++) { // Wait up to 60 seconds
            if (java.nio.file.Files.exists(hsHostname)) {
                onionAddress = new String(java.nio.file.Files.readAllBytes(hsHostname)).trim();
                hiddenServicePort = localPort;
                return onionAddress;
            }
            TimeUnit.SECONDS.sleep(1);
        }
        throw new IOException("Tor hidden service did not start in time");
    }

    /**
     * Stops the Tor process.
     */
    public void stop() {
        if (torProcess != null) {
            torProcess.destroy();
        }
    }

    public String getOnionAddress() {
        return onionAddress;
    }

    public int getHiddenServicePort() {
        return hiddenServicePort;
    }
}
