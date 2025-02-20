package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;

public class Server {

    private static final int PORT = 13726;
    private static final int HEARTBEAT_TIMEOUT = 10; // Seconds
    private static Map<String, ClientHandler> clientMap = new HashMap<>();
    private static ServerGUI gui;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            gui = new ServerGUI();
            gui.appendLog("Server starting...");
        });

        ExecutorService executor = Executors.newFixedThreadPool(10);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new InactiveClientChecker(), 0, HEARTBEAT_TIMEOUT * 1000);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);
            if (gui != null) {
                gui.appendLog("Server is running on port " + PORT);
            }

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientAddress = clientSocket.getInetAddress().getHostAddress();
                System.out.println("Client connected: " + clientAddress);

                if (gui != null) {
                    gui.appendLog("Client connected: " + clientAddress);
                    gui.addClient(clientAddress, "Возможно присоединиться", "N/A", "Online"); // Add client to GUI
                }

                ClientHandler clientHandler = new ClientHandler(clientSocket, clientAddress);
                clientMap.put(clientAddress, clientHandler);
                executor.submit(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            if (gui != null) {
                gui.appendLog("Server exception: " + e.getMessage());
            }
        } finally {
            executor.shutdown();
            timer.cancel();
            if (gui != null) {
                gui.removeAllClients(); // Clear the GUI if the server is shut down
            }
        }
    }

    public static ServerGUI getGui() {
        return gui;
    }

    private static class InactiveClientChecker extends TimerTask {
        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            for (Map.Entry<String, ClientHandler> entry : clientMap.entrySet()) {
                String clientAddress = entry.getKey();
                ClientHandler clientHandler = entry.getValue();
                if (currentTime - clientHandler.getLastHeartbeat() > HEARTBEAT_TIMEOUT * 1000) {
                    System.out.println("Client " + clientAddress + " is inactive. Removing...");
                    if (gui != null) {
                        gui.appendLog("Client " + clientAddress + " is inactive. Removing...");
                        gui.updateClientStatus(clientAddress, "Offline"); //Set Client Offline.
                    }
                    try {
                        Socket socket = clientHandler.getSocket();
                        if (socket != null && !socket.isClosed()) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        System.err.println("Error closing socket for client " + clientAddress + ": " + e.getMessage());
                        if (gui != null) {
                            gui.appendLog("Error closing socket for client " + clientAddress + ": " + e.getMessage());
                        }
                    } finally {
                        clientMap.remove(clientAddress);
                        if (gui != null) {
                            gui.removeClient(clientAddress); //Remove client from GUI

                        }
                    }
                }
            }
        }
    }
}