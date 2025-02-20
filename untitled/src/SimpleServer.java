import java.io.*;
import java.net.*;

public class SimpleServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(13726);
        System.out.println("Server started");
        Socket socket = serverSocket.accept();
        socket.setSoTimeout(30000); // 30 секунд
        System.out.println("Client connected");

        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("Server: Connection established");
            System.out.println("Sent initial message");

            String input = in.readLine();
            System.out.println("Received from client: " + input);

            out.println("Hello from server");
            System.out.println("Sent response to client - Before flush");
            out.flush();
            System.out.println("Sent response to client - After flush");


        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            socket.close();
            serverSocket.close();
            System.out.println("Connection closed");
        }
    }
}