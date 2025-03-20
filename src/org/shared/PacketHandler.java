package org.shared;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;

public class PacketHandler {
    private final Socket socket;
    private final SecretKey secretKey;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public PacketHandler(Socket socket, SecretKey secretKey) throws IOException {
        this.socket = socket;
        this.secretKey = secretKey;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    // Send an encrypted message
    public void sendPacket(String message) {
        try {
            String encryptedMessage = AESUtil.encrypt(message, secretKey);
            writer.println(encryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Receive and decrypt a message
    public String receivePacket() {
        try {
            String encryptedMessage = reader.readLine();
            return AESUtil.decrypt(encryptedMessage, secretKey);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() throws IOException {
        socket.close();
    }
}
