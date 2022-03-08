package lt.viko.eif.rcepauskas.chatserver;

import java.net.*;
import java.io.*;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public Server() {

    }

    public void start(int port) throws java.io.IOException {
        serverSocket = new ServerSocket(port);
        clientSocket = serverSocket.accept();
        System.out.println("Client connected");

        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        while (true) {
            if (in.ready()) {
                System.out.println(in.readLine());
            }
        }
    }

    public void stop() throws java.io.IOException {
        in.close();
        out.close();
        clientSocket.close();
        serverSocket.close();
    }
}
