# Encrypted Chat Application (Java)

## Overview

This is a **secure chat application** written in Java that allows multiple users to connect to a server and exchange encrypted messages. The encryption ensures that communication remains private and secure.

## How It Works

### **1. Server Setup**

- The server starts and generates an **RSA key pair** (public and private keys).
- It listens for incoming client connections on **port 5000**.
- When a client connects, the server **sends its public RSA key** to the client.

### **2. Client Connection & Key Exchange**

- Each client generates its own **AES-256 symmetric key**.
- The AES key is encrypted using the **server's RSA public key** and sent to the server.
- The server **decrypts the AES key** using its private RSA key and uses it to communicate securely with that client.

### **3. Secure Messaging**

- Clients **send and receive encrypted messages**.
- Messages are wrapped inside `PacketMessage` objects, which contain:
    - **content** (the actual encrypted message bytes)
    - **MessageType** (which can be `MESSAGE`, `FILE`, or `USERNAME`).
- The server acts as a **relay** between clients, forwarding messages to the intended recipients (TODO).

### **4. Username Transmission**

- Upon connecting, a client sends its **username** in a `PacketMessage` with `MessageType.USERNAME`.
- The server broadcasts this username to all connected clients.

## Project Structure

## Requirements

- **Java 8+**
- No external libraries required (uses built-in Java crypto APIs)

## How to Run

### **1. Start the Server**

The server will listen on **port 5000**.

### **2. Start a Client**

Each client will:

1. Receive the server's **public RSA key**.
2. Generate an **AES-256 symmetric key** and send it **encrypted** to the server.
3. Enter a **username**, which is broadcasted to all clients.
4. Start chatting securely!

## Security Features

✔ **RSA Key Exchange** – Prevents man-in-the-middle attacks.
✔ **AES-256 Encryption** – Strong symmetric encryption for secure messaging.
✔ **No Plaintext Transmission** – All communication is encrypted before sending.

## Future Enhancements

- Add user authentication.
- Implement secure file transfer.
- Improve message delivery reliability.

## License

This project is open-source and free to use. Contributions are welcome!

