public class ChatServer {
    public static void main(String[] args) throws InterruptedException {
        int port = 8818;
        Server server = new Server(port);
        server.start();
    }
}