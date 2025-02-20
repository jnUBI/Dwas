package client;
import java.io.*;
import java.net.*;

public class Client {

    private static final String SERVER_ADDRESS = "26.ip.gl.ply.gg";
    private static final int SERVER_PORT = 13726;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to server");

            out.println("Hello from client");
            out.flush();

            String response = in.readLine();
            System.out.println("Received: " + response);

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}