package lt.viko.eif.rcepauskas.chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();
    private List<String> onlineUsers = new CopyOnWriteArrayList<>();

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port);

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A new client connected");
                ClientHandler clientHandler = new ClientHandler(socket, clientHandlers, onlineUsers);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }
        catch (IOException ex) {
            System.out.println("Error in start: " + ex.getMessage());
        }
        finally {
            stop();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        }
        catch (IOException ex) {
            System.out.println("Error stopping server: " + ex.getMessage());
        }
    }

}
