//ClientHandler.java
package server;

import common.Command;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler implements Runnable {

    private Socket socket;
    private String clientAddress;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private long lastHeartbeat;
    private final ExecutorService commandExecutor = Executors.newFixedThreadPool(5); // Thread pool

    // Interface for Command Handlers
    interface CommandHandler {
        void handle(Object data) throws IOException, ClassNotFoundException;
    }

    private final Map<Command, CommandHandler> commandHandlers = new HashMap<>();

    public ClientHandler(Socket socket, String clientAddress) {
        this.socket = socket;
        this.clientAddress = clientAddress;
        this.lastHeartbeat = System.currentTimeMillis();

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error creating object streams: " + e.getMessage());
            logToGui("Error creating object streams: " + e.getMessage());
            closeConnection();
            return; // Important to exit if streams cannot be created
        }

        logToGui("Setting client " + clientAddress + " to Online");
        updateClientStatus("Online");
        initializeCommandHandlers();
    }

    private void initializeCommandHandlers() {
        commandHandlers.put(Command.HEARTBEAT, data -> {
            System.out.println("Received heartbeat from " + clientAddress);
            lastHeartbeat = System.currentTimeMillis();
            sendOK();
            logToGui("Received heartbeat from " + clientAddress);
        });

        commandHandlers.put(Command.SCREEN_RESPONSE, data -> {
            try {
                byte[] screenData = (byte[]) data;
                logToGui("Received screen data from " + clientAddress);
                ByteArrayInputStream bais = new ByteArrayInputStream(screenData);
                BufferedImage image = ImageIO.read(bais);

                if (image != null) {
                    ServerGUI gui = Server.getGui();
                    if (gui != null) {
                        gui.updateScreen(image);
                    } else {
                        System.out.println("GUI is null. Cannot update screen.");
                        logToGui("GUI is null. Cannot update screen.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error processing screen " + e.getMessage());
                logToGui("Error processing screen " + e.getMessage());
            }
        });

        commandHandlers.put(Command.WEBCAM_RESPONSE, data -> {
            try {
                byte[] webcamData = (byte[]) data;
                logToGui("Received webcam data from " + clientAddress);
                ByteArrayInputStream bais = new ByteArrayInputStream(webcamData);
                BufferedImage image = ImageIO.read(bais);

                if (image != null) {
                    ServerGUI gui = Server.getGui();
                    if (gui != null) {
                        gui.updateWebcam(image);
                    } else {
                        System.out.println("GUI is null. Cannot update webcam.");
                        logToGui("GUI is null. Cannot update webcam.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error processing webcam " + e.getMessage());
                logToGui("Error processing webcam " + e.getMessage());
            }
        });

        // Example: Adding a handler for a new command
        commandHandlers.put(Command.GET_WEBCAM_LIST, data -> {
            // Process the command to retrieve and send webcam list
            // Assuming you have a method to get the list
            String[] webcamList = getWebcamList();
            sendCommand(Command.WEBCAM_LIST, webcamList);
            logToGui("Sent webcam list to client " + clientAddress);
        });
    }

    @Override
    public void run() {
        try {
            Object input;
            while (socket.isConnected() && (in != null)) {
                try {
                    input = in.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error reading from client: " + e.getMessage());
                    logToGui("Error reading from client: " + e.getMessage());
                    break;
                }

                if (input == null) {
                    System.out.println("Client " + clientAddress + " disconnected.");
                    logToGui("Client " + clientAddress + " disconnected.");
                    break;
                }

                if (input instanceof Command) {
                    Command command = (Command) input;
                    commandExecutor.submit(() -> {
                        try {
                            Object data = in.readObject(); // Read data associated with the command
                            CommandHandler handler = commandHandlers.get(command);
                            if (handler != null) {
                                handler.handle(data);
                            } else {
                                System.out.println("Unknown command from " + clientAddress);
                                sendError("Unknown command");
                                logToGui("Unknown command from " + clientAddress);
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            System.err.println("Error processing command: " + e.getMessage());
                            logToGui("Error processing command: " + e.getMessage());
                        }
                    });
                } else {
                    System.out.println("Received unknown object from " + clientAddress);
                    sendError("Unknown object received");
                    logToGui("Received unknown object from " + clientAddress);
                }
            }
        } finally {
            logToGui("Setting client " + clientAddress + " to Offline");
            updateClientStatus("Offline");
            closeConnection();
            commandExecutor.shutdown(); // Shutdown the thread pool
        }
    }

    // Helper method to get the list of webcams
    private String[] getWebcamList() {
        // Implement your logic to retrieve the list of webcams
        return new String[]{"Webcam 1", "Webcam 2"}; // Example
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public Socket getSocket() {
        return socket;
    }

    private void sendCommand(Command command, Object data) {
        try {
            out.writeObject(command);
            out.writeObject(data); // Send the data along with the command
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending command " + command + " to client " + clientAddress + ": " + e.getMessage());
            logToGui("Error sending command " + command + " to client " + clientAddress + ": " + e.getMessage());
        }
    }

    private void sendOK() {
        sendCommand(Command.OK, null);
    }

    private void sendError(String message) {
        sendCommand(Command.ERROR, message);
    }

    private void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
            logToGui("Error closing connection: " + e.getMessage());
        }
    }

    private void logToGui(String message) {
        ServerGUI gui = Server.getGui();
        if (gui != null) {
            gui.appendLog(message);
        } else {
            System.out.println("GUI is null. Cannot append log: " + message);
        }
    }

    private void updateClientStatus(String status) {
        ServerGUI gui = Server.getGui();
        if (gui != null) {
            gui.updateClientStatus(clientAddress, status);
        } else {
            System.out.println("GUI is null. Cannot update client status.");
        }
    }
}