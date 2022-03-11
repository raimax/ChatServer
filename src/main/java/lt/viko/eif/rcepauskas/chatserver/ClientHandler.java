package lt.viko.eif.rcepauskas.chatserver;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    private static List<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket clientSocket;
    private BufferedReader in;
    private BufferedWriter out;
    private String clientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.clientSocket = socket;
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.clientUsername = in.readLine();
            clientHandlers.add(this);
            broadcastMessage("[Server] " + clientUsername + " has entered the chat");
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
                messageFromClient = in.readLine();
                broadcastMessage(clientUsername + ": " + messageFromClient);
            }
            catch (IOException e) {
                close();
                break;
            }
        }
    }

    private void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                clientHandler.out.write(message);
                clientHandler.out.newLine();
                clientHandler.out.flush();
            }
            catch (IOException e) {
               close();
            }
        }
    }

    private void close() {
        removeClientHandler();
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
}