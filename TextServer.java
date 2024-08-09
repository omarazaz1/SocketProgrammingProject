import java.io.*;
import java.net.*;
import java.util.*;

public class TextServer {
    private static final Map<String, String> userAccounts = new HashMap<>();
    private static final Map<String, List<String>> userMessages = new HashMap<>();

    public static void main(String[] args) {
        // Initialize user accounts and messages
        userAccounts.put("Alice", "1234");
        userAccounts.put("Bob", "5678");
        userMessages.put("Alice", new ArrayList<>());
        userMessages.put("Bob", new ArrayList<>());

        try (ServerSocket serverSocket = new ServerSocket(7000)) {
            System.out.println("Server started. Waiting for clients...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                if (authenticateUser()) {
                    showMenu();
                }
            } catch (IOException e) {
                System.err.println("Client communication error: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }

        private boolean authenticateUser() throws IOException {
            while (true) {
                out.println("Please enter the username:");
                out.flush(); // Ensure the client receives the message
                username = in.readLine();
                
                out.println("Please enter the password:");
                out.flush(); // Ensure the client receives the message
                String password = in.readLine();

                if (userAccounts.containsKey(username) && userAccounts.get(username).equals(password)) {
                    out.println("Access Granted");
                    out.flush();
                    System.out.println("User " + username + " authenticated successfully.");
                    return true;
                } else {
                    out.println("Access Denied – Username/Password Incorrect");
                    out.flush();
                    System.out.println("Failed login attempt for username: " + username);
                }
            }
        }

        private void showMenu() throws IOException {
            String choice;
            while (true) {
                 out.println("\nMenu:");
                out.println("1: Get the user list");
                  out.println("2: Send a message");
                out.println("3: Get my messages");
                out.println("4: Exit");
                out.println("Please enter a choice:");
                out.flush(); // Ensure the menu is sent to the client

                choice = in.readLine();
                if (choice == null) {
                    break;
                }

                switch (choice) {
                    case "1":
                        out.println("User list: " + String.join(", ", userAccounts.keySet()));
                        break;
                    case "2":
                        sendMessage();
                        break;
                    case "3":
                        getMessages();
                        break;
                    case "4":
                        out.println("Goodbye!");
                        out.flush();
                        return; // Exit the loop and end the connection
                    default:
                        out.println("Invalid option. Please try again.");
                        break;
                }
                out.flush(); // Ensure the response is sent to the client
            }
        }

        private void sendMessage() throws IOException {
            out.println("Enter recipient username:");
            out.flush();
            String recipient = in.readLine();

            if (userAccounts.containsKey(recipient)) {
                out.println("Enter your message:");
                out.flush();
                String message = in.readLine();
                userMessages.get(recipient).add(username + ": " + message);
                out.println("Message sent to " + recipient);
            } else {
                out.println("User not found.");
            }
            out.flush();
        }

        private void getMessages() throws IOException {
            List<String> messages = userMessages.getOrDefault(username, new ArrayList<>());
            if (messages.isEmpty()) {
                out.println("No new messages.");
            } else {
                out.println("Your messages:");
                for (String message : messages) {
                    out.println(message);
                }
                 messages.clear(); // Clear messages after reading
            }
            out.flush();
        }
    }
}
