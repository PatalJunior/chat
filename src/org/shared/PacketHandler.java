package org.shared;

import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.util.Base64;


public class PacketHandler {

    private final Socket socket;
    private final SecretKey secretKey;
    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;
    private boolean clientClosedConnection = false;
    public PacketHandler(Socket socket, SecretKey secretKey) throws IOException {
        this.socket = socket;
        this.secretKey = secretKey;
        this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        this.objectInputStream = new ObjectInputStream(socket.getInputStream());
    }

    // Send a PacketMessage
    public void sendPacket(PacketMessage packet) {
        try {
            // Encrypt content before sending
            byte[] encryptedContent = AESUtil.encrypt(packet.getContent(), secretKey);
            PacketMessage encryptedPacket = new PacketMessage(encryptedContent, packet.getType());

            if(this.clientClosedConnection) {
                System.out.println("Client closed connection, aborting packet");
                return;
            }

            objectOutputStream.writeObject(encryptedPacket);
            objectOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Receive and decrypt a PacketMessage
    public PacketMessage receivePacket() {
        try {
            if (socket.isClosed() || socket.isInputShutdown() || this.clientClosedConnection) {
                System.err.println("Socket is closed or input is shutdown.");
                return null;
            }

            PacketMessage receivedPacket = (PacketMessage) objectInputStream.readObject();

            // Decrypt content after receiving
            byte[] decryptedContent = AESUtil.decrypt(receivedPacket.getContent(), secretKey);
            return new PacketMessage(decryptedContent, receivedPacket.getType());

        } catch (Exception e) {
            return null;
        }


    }


    public void close() throws IOException {
        this.clientClosedConnection = true;
        socket.close();
    }
}
