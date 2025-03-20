package org.client;

import org.shared.AESUtil;
import org.shared.PacketHandler;
import org.shared.RSAUtil;

import java.io.*;
import java.net.Socket;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import java.util.Base64;

public class EncryptedClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 5000);
            System.out.println("Connected to server.");

            // Receive public key from the server
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            PublicKey serverPublicKey = (PublicKey) inputStream.readObject();

            // Generate AES key
            SecretKey aesKey = AESUtil.generateKey();

            // Encrypt AES key with RSA public key
            String aesKeyString = Base64.getEncoder().encodeToString(aesKey.getEncoded());
            String encryptedAESKey = RSAUtil.encrypt(aesKeyString, serverPublicKey);

            // Send encrypted AES key to server
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(encryptedAESKey);

            // Initialize PacketHandler
            PacketHandler packetHandler = new PacketHandler(socket, aesKey);

            // Send encrypted message
            packetHandler.sendPacket("Hello, server!");

            // Receive and decrypt server response
            String response = packetHandler.receivePacket();
            System.out.println("Decrypted Response from server: " + response);

            packetHandler.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
