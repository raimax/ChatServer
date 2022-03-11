package lt.viko.eif.rcepauskas.chatserver;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        server.start(6666);
    }
}
