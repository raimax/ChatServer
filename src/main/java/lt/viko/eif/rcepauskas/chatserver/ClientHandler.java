package lt.viko.eif.rcepauskas.chatserver;

import lt.viko.eif.rcepauskas.chatclient.SocketMessage;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable {

    private List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();
    private List<String> onlineUsers = new CopyOnWriteArrayList<>();
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.clientSocket = socket;
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());

            SocketMessage socketMessage = (SocketMessage) in.readObject();
            this.clientUsername = socketMessage.getMessage();

            broadcastMessage(SocketMessage.MessageType.MESSAGE, "[SERVER] " + clientUsername + " has entered the chat");
            clientHandlers.add(this);
            onlineUsers.add(clientUsername);
            broadcastOnlineUsers();
        }
        catch (IOException ex) {
            System.out.println("IOException in ClientHandler: " + ex.getMessage());
            close();
        }
        catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException in ClientHandler: " + ex.getMessage());
            close();
        }
    }

    @Override
    public void run() {
        SocketMessage messageFromClient;

        while (clientSocket.isConnected()) {
            try {
                if ((messageFromClient = (SocketMessage) in.readObject()) != null) {
                    broadcastMessage(
                            SocketMessage.MessageType.MESSAGE,
                            String.format("[%s] %s: %s", getCurrentTime(), clientUsername, messageFromClient.getMessage()));
                }
                else {
                    close();
                    break;
                }
            }
            catch (IOException | ClassNotFoundException e) {
                close();
                break;
            }
        }
    }

    private String getCurrentTime() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat timeNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return timeNow.format(timestamp);
    }

    private void broadcastMessage(SocketMessage.MessageType messageType, String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                clientHandler.out.writeObject(new SocketMessage(messageType, message));
            }
            catch (IOException ex) {
                System.out.println("Error broadcasting message: " + ex.getMessage());
            }
        }
    }

    private void broadcastOnlineUsers() {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                clientHandler.out.writeObject(new SocketMessage(SocketMessage.MessageType.ONLINE_USERS_LIST, onlineUsers));
            }
            catch (IOException ex) {
                System.out.println("Error broadcasting online users: " + ex.getMessage());
            }
        }
    }

    private void close() {
        removeClientHandler();
        removeOnlineUser(clientUsername);
        broadcastOnlineUsers();
        try {
            in.close();
            out.close();
            clientSocket.close();
        }
        catch (IOException ex) {
            System.out.println("Error closing socket: " + ex.getMessage());
        }
    }

    private void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage(SocketMessage.MessageType.MESSAGE, "[SERVER] " + clientUsername + " has left the chat");
    }

    private void removeOnlineUser(String username) {
        onlineUsers.remove(username);
    }
}
