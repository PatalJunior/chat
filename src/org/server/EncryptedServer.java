package org.server;
import org.shared.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EncryptedServer {
    private static final PublicKey serverPublicKey;
    private static final PrivateKey serverPrivateKey;

    // Store connected clients (socket -> handler)
    private static final Set<ClientHandler> connectedClients = Collections.newSetFromMap(new ConcurrentHashMap<>());

    static {
        try {
            KeyPair rsaKeyPair = RSAUtil.generateKeyPair();
            serverPublicKey = rsaKeyPair.getPublic();
            serverPrivateKey = rsaKeyPair.getPrivate();
        } catch (Exception e) {
            throw new RuntimeException("Error generating RSA key pair", e);
        }
    }

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Server listening on port 5000...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, serverPublicKey, serverPrivateKey);

                connectedClients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Broadcasts a username message to all connected clients except the sender
    public static void broadcastUsername(String username, ClientHandler sender) {
        for (ClientHandler client : connectedClients) {
            if (client != sender) {
                client.sendUsername(username);
            }
        }
    }

    // Remove a client from the active clients list
    public static void removeClient(ClientHandler client) {
        connectedClients.remove(client);
    }
}

