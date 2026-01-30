# Architecture — cwtch-java-protocol

## Overview
This document describes the architecture and design of the Cwtch-style decentralized messaging protocol implemented in this library.

### Key Components
- **Identity**: Ed25519/X25519 key pair for each user
- **Tor Integration**: Each client runs a Tor hidden service (onion address)
- **Handshake**: ECDH key exchange for session key negotiation
- **Messaging**: All messages encrypted with session key (AES-GCM/ChaCha20)
- **Group Chat**: Group key management and secure distribution

## Protocol Flow

```mermaid

sequenceDiagram
    participant Alice as Alice (Onion Address, Ed25519 Key)
    participant Bob as Bob (Onion Address, Ed25519 Key)
    participant Tor as Tor Network

    Alice->>Tor: Start Tor client, create hidden service (onion address)
    Bob->>Tor: Start Tor client, create hidden service (onion address)

    Alice->>Bob: Exchange onion address + public key (out-of-band)
    Bob->>Alice: Exchange onion address + public key (out-of-band)

    Alice->>Tor: Connect to Bob's onion address
    Tor->>Bob: Route connection to Bob's hidden service
    Alice->>Bob: Initiate handshake (send ephemeral key)
    Bob->>Alice: Respond with ephemeral key
    Alice->>Bob: Both derive session key (ECDH)

    Alice->>Bob: Send encrypted message (AES-GCM/ChaCha20)
    Bob->>Alice: Send encrypted message (AES-GCM/ChaCha20)

    Note over Alice,Bob: All traffic is routed through Tor, no metadata is leaked
```

## Security Model
- No central server or directory
- Onion addresses and keys exchanged out-of-band
- All traffic routed through Tor for anonymity
- Forward secrecy via ephemeral session keys
- No cleartext metadata (usernames, contact lists, etc.)

---

See [api.md](api.md) for class-level documentation and [diagrams.md](diagrams.md) for more protocol flows.
