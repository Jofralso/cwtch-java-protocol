
# cwtch-java-protocol

A Java library for decentralized, metadata-resistant, end-to-end encrypted messaging using Tor hidden services, inspired by the Cwtch protocol.

---

## Features
- Decentralized, serverless architecture
- Tor hidden service integration for anonymous addressing
- End-to-end encryption (Ed25519/X25519, AES-GCM/ChaCha20)
- Metadata resistance: no central directory, no cleartext contact lists
- Secure handshake and session key negotiation
- Group chat support with group key management
- Designed for extensibility and auditability

---

## Quickstart

### Prerequisites
- Java 17 or newer
- Maven
- Tor installed and available on your system PATH

### Setup
1. **Clone the repository:**
	```sh
	git clone <repo-url>
	cd cwtch-java
	```
2. **Build the project:**
	```sh
	mvn clean package
	```
3. **Run the Tor integration example:**
	```sh
	mvn compile exec:java -Dexec.mainClass=protocol.TorIntegrationExample
	```
	Ensure Tor is running with ControlPort enabled (see below).

### Using the Library
1. **Add to your Maven project:**
	- Copy the relevant dependencies from this repo's `pom.xml`.
2. **Generate a user identity:**
	```java
	Identity id = Identity.generate();
	System.out.println("Public Key: " + id.getPublicKeyBase64());
	```
3. **Start a Tor hidden service:**
	```java
	TorManager tor = new TorManager();
	String onion = tor.startHiddenService(9001);
	System.out.println("My onion address: " + onion);
	```
4. **Connect and send messages:**
	- Exchange onion addresses and public keys with contacts (out-of-band).
	- Use `PeerChannel` and `ProtocolMessage` to send encrypted messages.

---

## Documentation

- [Architecture & Security Model](docs/architecture.md)
- [API Reference & Examples](docs/api.md)
- [Project Documentation Index](docs/README.md)

---

## Tor Setup

Make sure Tor is running with the following in `/etc/tor/torrc`:
```
ControlPort 9051
CookieAuthentication 1
```
Restart Tor after editing. Your user must be able to read `/var/run/tor/control.authcookie` (see docs/api.md for troubleshooting).

---

## Resources

- [Cwtch Protocol (original)](https://cwtch.im/)
- [Tor Project Documentation](https://2019.www.torproject.org/docs/documentation.html.en)
- [BouncyCastle Java Crypto](https://www.bouncycastle.org/java.html)
- [Java Socket Programming](https://docs.oracle.com/javase/tutorial/networking/sockets/)

---

## License

Apache License 2.0

---

This project is open source and welcomes contributions. See CONTRIBUTING.md for guidelines.
