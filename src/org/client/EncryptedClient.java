package org.client;

import org.shared.*;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Scanner;

public class EncryptedClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 5000);
            System.out.println("Connecting to server.");

            // Receive public key from the server
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            PublicKey serverPublicKey = (PublicKey) inputStream.readObject();
            System.out.println("Received server public key.");

            // Generate AES key
            SecretKey aesKey = AESUtil.generateKey();
            System.out.println("Generating client AES key.");

            // Encrypt AES key with RSA public key
            String aesKeyString = Base64.getEncoder().encodeToString(aesKey.getEncoded());
            String encryptedAESKey = RSAUtil.encrypt(aesKeyString, serverPublicKey);
            System.out.println("Encrypting AES key with server public key.");

            // Send encrypted AES key to server
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(encryptedAESKey);
            System.out.println("Encrypted AES key sent to server.");

            // Initialize PacketHandler
            PacketHandler packetHandler = new PacketHandler(socket, aesKey);
            System.out.println("Initializing packet handler with client AES key.");

            // Ask user for a username
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

            // Send the username to the server
            packetHandler.sendPacket(new PacketMessage(username.getBytes(), MessageType.USERNAME));
            System.out.println("Username sent to server: " + username);

            // Create a receiver task
            Runnable receiverTask = new ReceiverTask(socket, packetHandler);
            Thread receiveThread = new Thread(receiverTask);
            receiveThread.start();
            System.out.println("Starting receiver task thread.");

            // Allow user input to send messages
            while (true) {
                System.out.print("Enter message (or type 'exit' to quit): ");
                String messageToSend = scanner.nextLine();

                if (messageToSend.equalsIgnoreCase("exit")) {
                    break;
                }

                packetHandler.sendPacket(new PacketMessage(messageToSend.getBytes(), MessageType.MESSAGE));
            }

            // Cleanup
            socket.close();
            packetHandler.close();
            receiveThread.interrupt();
            System.out.println("Client disconnected.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ReceiverTask implementing Runnable to handle incoming messages
    static class ReceiverTask implements Runnable {
        private Socket socket;
        private PacketHandler packetHandler;

        public ReceiverTask(Socket socket, PacketHandler packetHandler) {
            this.socket = socket;
            this.packetHandler = packetHandler;
        }

        @Override
        public void run() {
            try {
                while (!socket.isClosed()) {
                    PacketMessage message = packetHandler.receivePacket();
                    if (message != null) {
                        switch (message.getType()) {
                            case MESSAGE:
                                System.out.println("Message: " + new String(message.getContent()));
                                break;
                            case USERNAME:
                                System.out.println("New user joined: " + new String(message.getContent()));
                                break;
                            default:
                                System.out.println("Unknown packet type received.");
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Connection closed or error receiving message.");
            }
        }
    }
}
