package protocol;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * TorControlClient manages a connection to the Tor ControlPort using plain sockets.
 * It can authenticate, create ephemeral hidden services, and send commands.
 *
 * This is the recommended way to control Tor from Java (best practice).
 */
public class TorControlClient implements Closeable {
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;


    /**
     * Create a TorControlClient connecting to the given host/port.
     */
    public TorControlClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    /**
     * Try to authenticate using CookieAuthentication (recommended),
     * falling back to no-auth if cookie not found.
     *
     * @param cookiePath Path to Tor's control.authcookie (null to auto-detect)
     */
    public void authenticateWithCookie(String cookiePath) throws IOException {
        File cookieFile = null;
        if (cookiePath != null) {
            cookieFile = new File(cookiePath);
        } else {
            // Try common locations
            String[] paths = {
                "/var/run/tor/control.authcookie",
                "/run/tor/control.authcookie",
                System.getProperty("user.home") + "/.tor/control_auth_cookie"
            };
            for (String p : paths) {
                File f = new File(p);
                if (f.exists()) {
                    cookieFile = f;
                    break;
                }
            }
        }
        if (cookieFile != null && cookieFile.exists()) {
            byte[] cookie = readAllBytes(cookieFile);
            StringBuilder hex = new StringBuilder();
            for (byte b : cookie) {
                hex.append(String.format("%02X", b));
            }
            send("AUTHENTICATE " + hex + "\r\n");
            String resp = in.readLine();
            if (!resp.startsWith("250")) {
                throw new IOException("Tor cookie authentication failed: " + resp);
            }
        } else {
            // Fallback: try no-auth (for default config)
            authenticate();
        }
    }

    private static byte[] readAllBytes(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[64];
            int r;
            while ((r = fis.read(buf)) != -1) {
                bos.write(buf, 0, r);
            }
            return bos.toByteArray();
        }
    }

    /**
     * Authenticate with the Tor ControlPort (no password for default config).
     * If CookieAuthentication is enabled, use authenticateWithCookie instead.
     */
    public void authenticate() throws IOException {
        send("AUTHENTICATE\r\n");
        String resp = in.readLine();
        if (!resp.startsWith("250")) {
            throw new IOException("Tor authentication failed: " + resp);
        }
    }

    /**
     * Create an ephemeral hidden service (v3) on the given port.
     * Returns the onion address.
     */
    public String addOnion(int virtPort, int targetPort) throws IOException {
        send("ADD_ONION NEW:ED25519-V3 Port=" + virtPort + ",127.0.0.1:" + targetPort + "\r\n");
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = in.readLine()) != null && !line.equals("250 OK")) {
            lines.add(line);
        }
        for (String l : lines) {
            if (l.startsWith("250-ServiceID=")) {
                return l.substring("250-ServiceID=".length()) + ".onion";
            }
        }
        throw new IOException("Failed to create onion service: " + lines);
    }

    /**
     * Send a raw command to the Tor ControlPort.
     */
    public void send(String cmd) {
        out.print(cmd);
        out.flush();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
