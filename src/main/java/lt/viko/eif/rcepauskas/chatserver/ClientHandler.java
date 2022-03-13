package lt.viko.eif.rcepauskas.chatserver;

import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientHandler implements Runnable {

    private static List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();
    private static List<String> onlineUsers = new CopyOnWriteArrayList<>();
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.clientSocket = socket;
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.clientUsername = in.readLine();
            broadcastMessage("[SERVER] " + clientUsername + " has entered the chat");
            clientHandlers.add(this);
            onlineUsers.add(clientUsername);
        }
        catch (IOException e) {
            close();
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (clientSocket.isConnected()) {
            try {
                if ((messageFromClient = in.readLine()) != null) {
                    broadcastMessage(String.format("[%s] %s: %s", getCurrentTime(), clientUsername, messageFromClient));
                }
                else {
                    close();
                    break;
                }
            }
            catch (IOException e) {
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

    private void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.out.println(message);
        }
    }

    private void close() {
        removeClientHandler();
        removeOnlineUser(clientUsername);
        try {
            in.close();
            out.close();
            clientSocket.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("[SERVER] " + clientUsername + " has left the chat");
    }

    private void removeOnlineUser(String username) {
        onlineUsers.remove(username);
    }
}
