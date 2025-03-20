package org.server;

import org.shared.*;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

// Handles a single client in a separate thread
public class ClientHandler implements Runnable {
    private final Socket socket;
    private PacketHandler packetHandler;
    private final PublicKey serverPublicKey;
    private final PrivateKey serverPrivateKey;
    private String username; // Store client’s username

    public ClientHandler(Socket socket, PublicKey serverPublicKey, PrivateKey serverPrivateKey) {
        this.socket = socket;
        this.serverPublicKey = serverPublicKey;
        this.serverPrivateKey = serverPrivateKey;
    }

    @Override
    public void run() {
        try {
            // Key exchange phase
            ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(this.serverPublicKey);
            outputStream.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String encryptedAESKey = reader.readLine();

            if (encryptedAESKey == null) {
                System.out.println("Client disconnected before key exchange.");
                closeConnection();
                return;
            }

            String aesKeyString = RSAUtil.decrypt(encryptedAESKey, this.serverPrivateKey);
            SecretKey aesKey = AESUtil.getKeyFromBytes(Base64.getDecoder().decode(aesKeyString));

            this.packetHandler = new PacketHandler(socket, aesKey);

            // Communication loop
            while (true) {
                PacketMessage packet = packetHandler.receivePacket();
                if (packet == null) {
                    System.out.println("Client disconnected.");
                    break;
                }

                switch (packet.getType()) {
                    case MESSAGE:
                        System.out.println("[" + username + "]: " + new String(packet.getContent()));
                        break;

                    case USERNAME:
                        this.username = new String(packet.getContent()); // Store the username
                        System.out.println("User joined: " + username);
                        EncryptedServer.broadcastUsername(username, this);
                        break;

                    default:
                        System.err.println("Unknown packet type received.");
                }
            }
        } catch (IOException e) {
            System.out.println("Client connection lost: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public void sendUsername(String username) {
        try {
            PacketMessage usernamePacket = new PacketMessage(username.getBytes(), MessageType.USERNAME);
            packetHandler.sendPacket(usernamePacket);
        } catch (Exception e) {
            System.err.println("Failed to send username: " + e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            EncryptedServer.removeClient(this);
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