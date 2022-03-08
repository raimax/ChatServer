package lt.viko.eif.rcepauskas.chatserver;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();

        try {
            server.start(6969);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
