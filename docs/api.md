# Identity Class — cwtch-java-protocol

The `Identity` class represents a user's long-term cryptographic identity for the protocol.

## Features
- Generates an Ed25519 key pair (using BouncyCastle)
- Provides access to public/private keys
- Supports Base64 serialization for sharing and storage
- Designed for use in authentication, session establishment, and onion address generation

## Example Usage
```java
import protocol.Identity;

// Generate a new identity
Identity id = Identity.generate();
System.out.println("Public Key: " + id.getPublicKeyBase64());
System.out.println("Private Key: " + id.getPrivateKeyBase64());
```

## Next Steps
- Add secure key storage/loading methods
- Integrate with Tor hidden service address generation
- Use for handshake and session establishment

# TorManager Class — cwtch-java-protocol

The `TorManager` class manages launching a Tor process, creating a hidden service, and retrieving the onion address for the client.

## Features
- Launches a Tor process with a temporary data directory and torrc
- Creates a hidden service on a specified local port
- Waits for the onion address to become available
- Provides methods to stop Tor and retrieve the onion address/port

## Example Usage
```java
import protocol.TorManager;

TorManager tor = new TorManager();
String onion = tor.startHiddenService(9001);
System.out.println("My onion address: " + onion);
// ...
tor.stop();
```

## Notes
- Assumes Tor is installed and available on the system PATH
- For production, bundle Tor or use a Java Tor library for better portability
- The hidden service directory and torrc are created in a temporary folder

## Next Steps
- Integrate with Identity for address binding
- Add error handling and logging
- Support for persistent hidden services

# Handshake Class — cwtch-java-protocol

The `Handshake` class implements the X25519 ECDH key exchange for secure session key negotiation between peers.

## Features
- Generates ephemeral X25519 key pairs for each session
- Computes shared secret using ECDH
- Encodes/decodes public keys as Base64 for transmission
- Uses BouncyCastle for cryptography

## Example Usage
```java
import protocol.Handshake;
import java.security.KeyPair;

// Generate ephemeral key pair
KeyPair myEphemeral = Handshake.generateEphemeralKeyPair();
// Exchange public keys with peer (Base64)
String myPub = Handshake.encodePublicKey(myEphemeral.getPublic());
// ... receive peerPub from network ...
// Compute shared secret
byte[] sharedSecret = Handshake.computeSharedSecret(myEphemeral.getPrivate(), peerEphemeralPubKey);
```

## Notes
- The shared secret should be used as input to a KDF (e.g., HKDF) to derive session keys for encryption
- Public keys are exchanged at the start of the connection (see protocol diagram)

## Next Steps
- Integrate with message encryption (AES-GCM/ChaCha20)
- Add HKDF-based key derivation for session keys

# SessionCrypto Class — cwtch-java-protocol

The `SessionCrypto` class handles session key derivation (HKDF) and message encryption/decryption (AES-GCM) for secure communication.

## Features
- Derives a 256-bit AES session key from the ECDH shared secret using HKDF (SHA-256)
- Encrypts and decrypts messages using AES-GCM (with random IV)
- Uses BouncyCastle for cryptography

## Example Usage
```java
import protocol.SessionCrypto;
import javax.crypto.SecretKey;

// Derive session key
SecretKey sessionKey = SessionCrypto.deriveSessionKey(sharedSecret, salt, info);
// Encrypt a message
String ciphertext = SessionCrypto.encrypt("Hello, world!", sessionKey);
// Decrypt a message
String plaintext = SessionCrypto.decrypt(ciphertext, sessionKey);
```

## Notes
- Use a random salt and context-specific info for HKDF
- IV is randomly generated and prepended to the ciphertext
- AES-GCM provides confidentiality and integrity

## Next Steps
- Integrate with Handshake for full session establishment
- Add message authentication and replay protection

# ProtocolMessage Class — cwtch-java-protocol

The `ProtocolMessage` class defines the structure for all messages exchanged between peers in the protocol.

## Features
- Minimal fields: type, encrypted payload, message counter
- Compact serialization/deserialization for transmission
- Designed to minimize metadata exposure

## Example Usage
```java
import protocol.ProtocolMessage;

// Create a message
ProtocolMessage msg = new ProtocolMessage("chat", encryptedPayload, 1);
String serialized = msg.serialize();
// ... send over network ...
// Parse received message
ProtocolMessage received = ProtocolMessage.deserialize(serialized);
```

## Notes
- The payload should always be encrypted (e.g., with SessionCrypto)
- The counter can be used for replay protection and ordering
- Message type can be "handshake", "chat", "group", etc.

## Next Steps
- Integrate with peer-to-peer communication logic
- Add support for group messages and attachments

# PeerChannel Class — cwtch-java-protocol

The `PeerChannel` class manages a peer-to-peer connection over a socket (e.g., via Tor hidden service), handling encrypted ProtocolMessages.

## Features
- Sends and receives ProtocolMessages with encrypted payloads
- Uses a session key (from Handshake/HKDF) for AES-GCM encryption
- Maintains message counters for replay protection
- Simple API for sending and receiving messages

## Example Usage
```java
import protocol.PeerChannel;
import javax.crypto.SecretKey;
import java.net.Socket;

PeerChannel channel = new PeerChannel(socket, sessionKey);
channel.send("chat", "Hello, world!");
ProtocolMessage msg = channel.receive();
System.out.println("Received: " + msg.getType() + ": " + msg.getPayload());
channel.close();
```

## Notes
- Designed for use with Tor hidden service sockets
- Message counters help prevent replay attacks and ensure ordering
- Integrate with higher-level protocol logic for handshake and group chat

## Next Steps
- Add support for group messaging and attachments
- Integrate with application event loop and error handling

# TorControlClient Class — cwtch-java-protocol

The `TorControlClient` class manages a connection to the Tor ControlPort using plain sockets, following best practice for Tor integration in Java.

## Features
- Connects to Tor's ControlPort (default 127.0.0.1:9051)
- Authenticates (default: no password)
- Creates ephemeral v3 onion services (hidden services)
- Sends raw commands to Tor
- No external dependencies required

## Example Usage
```java
import protocol.TorControlClient;

TorControlClient tor = new TorControlClient("127.0.0.1", 9051);
tor.authenticate();
String onion = tor.addOnion(9001, 9001);
System.out.println("Onion address: " + onion);
tor.close();
```

## Notes
- Tor must be running with ControlPort enabled (e.g., `tor --ControlPort 9051`)
- For production, use a strong authentication cookie or password
- This approach is robust, cross-platform, and recommended by the Tor Project

## Next Steps
- Integrate with TorManager to launch Tor and manage ControlPort
- Add support for authentication cookies and advanced commands

# Tor Integration Example — cwtch-java-protocol

This example demonstrates how to launch Tor, connect to the ControlPort, create a hidden service, and print the onion address using TorControlClient.

## Example Code
```java
import protocol.TorControlClient;

// Launch Tor as an external process (ensure tor is installed and on PATH)
Process torProcess = new ProcessBuilder("tor", "--ControlPort", "9051").start();
Thread.sleep(5000); // Wait for Tor to start

try (TorControlClient tor = new TorControlClient("127.0.0.1", 9051)) {
    tor.authenticate();
    String onion = tor.addOnion(9001, 9001);
    System.out.println("Onion address: " + onion);
}
torProcess.destroy();
```

## Notes
- Make sure Tor is installed and available on the system PATH
- In production, check for Tor readiness instead of using Thread.sleep
- You can now use the onion address for peer-to-peer connections

---

See TorControlClient and TorManager for more advanced usage.
