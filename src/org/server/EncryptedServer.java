package org.server;
import org.shared.AESUtil;
import org.shared.PacketHandler;
import org.shared.RSAUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import javax.crypto.SecretKey;

public class EncryptedServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Server listening on port 5000...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected.");

                // Handle each client in a new thread
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

// Handles a single client in a separate thread
class ClientHandler implements Runnable {
    private final Socket socket;
    private SecretKey aesKey;
    private PacketHandler packetHandler;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    public ClientHandler(Socket socket) {
        this.socket = socket;

        try {
            // Generate RSA key pair for each client
            KeyPair rsaKeyPair = RSAUtil.generateKeyPair();
            this.privateKey = rsaKeyPair.getPrivate();
            this.publicKey = rsaKeyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException("Error generating RSA key pair", e);
        }
    }

    @Override
    public void run() {
        try {
            // Key exchange phase
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(publicKey);
            outputStream.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String encryptedAESKey = reader.readLine();

            if (encryptedAESKey == null) {
                System.out.println("Client disconnected before key exchange.");
                closeConnection();
                return;
            }

            // Decrypt AES key
            String aesKeyString = RSAUtil.decrypt(encryptedAESKey, privateKey);
            this.aesKey = AESUtil.getKeyFromBytes(Base64.getDecoder().decode(aesKeyString));

            // Initialize PacketHandler
            this.packetHandler = new PacketHandler(socket, aesKey);

            // Communication loop
            while (true) {
                String message = packetHandler.receivePacket();

                if (message == null || message.equalsIgnoreCase("exit")) {
                    System.out.println("Client disconnected.");
                    break;
                }

                System.out.println("Received: " + message);
                packetHandler.sendPacket("Server received: " + message);
            }
        } catch (IOException e) {
            System.out.println("Client connection lost: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if (packetHandler != null) {
                packetHandler.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Connection closed.");
        } catch (IOException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
}
