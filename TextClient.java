import java.io.*;
import java.net.*;

public class TextClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("127.0.0.1", 7000);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            String fromServer;
            String fromUser;

            // Authentication
            while (true) {
                fromServer = in.readLine();
                if (fromServer == null) {
                    System.out.println("Server closed connection.");
                    break;
                }
                System.out.println(fromServer);
                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    out.println(fromUser);
                    out.flush();
                }
                if ("Access Granted".equals(fromServer)) {
                    break;
                }
            }

            // Menu and Commands
            while (true) {
                fromServer = in.readLine();
                if (fromServer == null) {
                    System.out.println("Server closed connection.");
                    break;
                }
                System.out.println(fromServer);

                fromUser = stdIn.readLine();
                if (fromUser != null) {
                    out.println(fromUser);
                    out.flush();
                }

                if ("Goodbye!".equals(fromServer)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Couldn't connect to the server: " + e.getMessage());
        }
    }
}
