package server;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import com.github.sarxos.webcam.Webcam;

public class ServerGUI extends JFrame {

    private JTextArea logArea;
    private JButton controlMouse, controlKeyboard, uploadFiles, downloadFiles, manageProcesses,
            startAudio, stopAudio, startWebcam, stopWebcam; // New Buttons
    private JLabel screenLabel;
    private JPanel buttonPanel;
    private JPanel screenPanel;

    private JTable clientTable;
    private DefaultTableModel tableModel;
    private JLabel onlineCountLabel, offlineCountLabel, totalCountLabel;

    private List<ClientInfo> clientList = new ArrayList<>();

    // Dark Color Scheme
    private Color backgroundColor = new Color(40, 40, 40);
    private Color textColor = new Color(220, 220, 220);
    private Color buttonColor = new Color(60, 60, 60);
    private Color borderColor = new Color(80, 80, 80);

    private JPanel audioPanel;
    private JPanel webcamPanel;
    private JLabel webcamLabel;

    private JComboBox<Integer> screenComboBox;
    private JComboBox<String> microphoneComboBox;
    private JComboBox<String> webcamComboBox;

    private String selectedWebcam = "Default Webcam";

    public ServerGUI() {
        setTitle("Remote Control Server");
        setSize(800, 600); // Set to 800x600
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main panel
        JPanel mainPanel = new JPanel();
        GroupLayout layout = new GroupLayout(mainPanel);
        mainPanel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        add(mainPanel, BorderLayout.CENTER);

        mainPanel.setBackground(backgroundColor);
        // Components

        // Client Table
        String[] columnNames = {"IP Address", "Подключение", "Окно", "Статус"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        clientTable = new JTable(tableModel);
        applyDarkThemeToTable(clientTable);

        JTableHeader header = clientTable.getTableHeader();
        header.setReorderingAllowed(false);
        clientTable.setRowSelectionAllowed(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < clientTable.getColumnCount(); i++) {
            clientTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        clientTable.setRowSorter(sorter);

        JScrollPane tableScrollPane = new JScrollPane(clientTable);
        tableScrollPane.setPreferredSize(new Dimension(getWidth(), 100)); // Reduced Height
        tableScrollPane.getViewport().setBackground(backgroundColor);

        // Load Icons (Remove this section)
        /*ImageIcon mouseIcon = createImageIcon("/icons/mouse.png");
        ImageIcon keyboardIcon = createImageIcon("/icons/keyboard.png");
        ImageIcon uploadIcon = createImageIcon("/icons/upload.png");
        ImageIcon downloadIcon = createImageIcon("/icons/process.png");
        */

        controlMouse = createButton("Мышь", null);
        controlKeyboard = createButton("Клавиатура", null);
        uploadFiles = createButton("Загрузить", null);
        downloadFiles = createButton("Выгрузить", null);
        manageProcesses = createButton("Процессы", null);

        // Statistics Panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        statsPanel.setBackground(backgroundColor);
        onlineCountLabel = new JLabel("В сети: 0");
        offlineCountLabel = new JLabel(" | Не в сети: 0");
        totalCountLabel = new JLabel(" | Всего: 0");

        Font statsFont = new Font("Arial", Font.BOLD, 10); // Reduced font size
        onlineCountLabel.setFont(statsFont);
        onlineCountLabel.setForeground(textColor);
        offlineCountLabel.setFont(statsFont);
        offlineCountLabel.setForeground(textColor);
        totalCountLabel.setFont(statsFont);
        totalCountLabel.setForeground(textColor);

        statsPanel.add(onlineCountLabel);
        statsPanel.add(offlineCountLabel);
        statsPanel.add(totalCountLabel);

        // Screen Panel
        screenPanel = new JPanel(new BorderLayout());
        screenPanel.setBorder(BorderFactory.createLineBorder(borderColor));
        screenPanel.setBackground(backgroundColor);
        screenLabel = new JLabel();
        screenLabel.setHorizontalAlignment(SwingConstants.CENTER);
        screenLabel.setForeground(textColor);
        screenPanel.add(screenLabel, BorderLayout.CENTER);
        screenPanel.setPreferredSize(new Dimension(400, 200)); // Reduced Screen Size

        // Audio Panel
        audioPanel = new JPanel();
        audioPanel.setBackground(backgroundColor);

        startAudio = createButton("Микрофон ON", null);
        stopAudio = createButton("Микрофон OFF", null);
        stopAudio.setEnabled(false); // Initially Disabled
        audioPanel.add(startAudio);
        audioPanel.add(stopAudio);

        // Webcam Panel
        webcamPanel = new JPanel(new BorderLayout());
        webcamPanel.setBackground(backgroundColor);
        webcamLabel = new JLabel();
        webcamLabel.setHorizontalAlignment(SwingConstants.CENTER);
        webcamLabel.setForeground(textColor);
        webcamPanel.add(webcamLabel, BorderLayout.CENTER);
        webcamPanel.setPreferredSize(new Dimension(400, 200)); // Reduced webcam size

        startWebcam = createButton("Веб-камера ON", null);
        stopWebcam = createButton("Веб-камера OFF", null);
        stopWebcam.setEnabled(false); // Initially Disabled
        webcamPanel.add(startWebcam, BorderLayout.SOUTH);
        webcamPanel.add(stopWebcam, BorderLayout.NORTH);

        // Device Selection Panel
        JPanel deviceSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        deviceSelectionPanel.setBackground(backgroundColor);

        // Screen Selection ComboBox
        Integer[] screenOptions = {0, 1, 2}; // Replace with actual screen indices
        screenComboBox = new JComboBox<>(screenOptions);
        screenComboBox.setToolTipText("Выберите экран");
        deviceSelectionPanel.add(new JLabel("Экран: "));
        deviceSelectionPanel.add(screenComboBox);

        // Microphone Selection ComboBox
        String[] microphoneOptions = getMicrophoneList();
        microphoneComboBox = new JComboBox<>(microphoneOptions);
        microphoneComboBox.setToolTipText("Выберите микрофон");
        deviceSelectionPanel.add(new JLabel("Микрофон: "));
        deviceSelectionPanel.add(microphoneComboBox);

        // Webcam Selection ComboBox
        String[] webcamOptions = getWebcamList();
        webcamComboBox = new JComboBox<>(webcamOptions);
        webcamComboBox.setToolTipText("Выберите веб-камеру");
        deviceSelectionPanel.add(new JLabel("Веб-камера: "));
        deviceSelectionPanel.add(webcamComboBox);

        webcamComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedWebcam = (String) webcamComboBox.getSelectedItem();
                logArea.append("Selected Webcam: " + selectedWebcam + "\n");
                // Notify the client to switch to the selected webcam
            }
        });

        // Log Area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBackground(backgroundColor);
        logArea.setForeground(textColor);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(getWidth(), 70)); // More reduced Log area height
        logScrollPane.getViewport().setBackground(backgroundColor);

        // Action Listeners (example actions)
        controlMouse.addActionListener(e -> appendLog("Мышь"));
        controlKeyboard.addActionListener(e -> appendLog("Клавиатура"));
        uploadFiles.addActionListener(e -> appendLog("Загрузить"));
        downloadFiles.addActionListener(e -> appendLog("Выгрузить"));
        manageProcesses.addActionListener(e -> appendLog("Процессы"));

        startAudio.addActionListener(e -> {
            appendLog("Микрофон ON");
            startAudio.setEnabled(false);
            stopAudio.setEnabled(true);
        });

        stopAudio.addActionListener(e -> {
            appendLog("Микрофон OFF");
            startAudio.setEnabled(true);
            stopAudio.setEnabled(false);
        });

        startWebcam.addActionListener(e -> {
            appendLog("Веб-камера ON");
            startWebcam.setEnabled(false);
            stopWebcam.setEnabled(true);
        });

        stopWebcam.addActionListener(e -> {
            appendLog("Веб-камера OFF");
            startWebcam.setEnabled(true);
            stopWebcam.setEnabled(false);
        });

        // GroupLayout

        // Horizontal Group
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(controlMouse)
                        .addComponent(controlKeyboard)
                        .addComponent(uploadFiles)
                        .addComponent(downloadFiles)
                        .addComponent(manageProcesses)
                        .addComponent(startAudio)
                        .addComponent(stopAudio))
                .addGroup(layout.createParallelGroup()
                        .addComponent(tableScrollPane)
                        .addComponent(statsPanel)
                        .addComponent(deviceSelectionPanel) // Device Selection
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(screenPanel)
                                .addComponent(webcamPanel))
                        .addComponent(logScrollPane)));

        // Vertical Group
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(controlMouse)
                                .addComponent(controlKeyboard)
                                .addComponent(uploadFiles)
                                .addComponent(downloadFiles)
                                .addComponent(manageProcesses)
                                .addComponent(startAudio)
                                .addComponent(stopAudio))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(tableScrollPane)
                                .addComponent(statsPanel)
                                .addComponent(deviceSelectionPanel) //Device Selection
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                        .addComponent(screenPanel)
                                        .addComponent(webcamPanel))
                                .addComponent(logScrollPane)))
        );

        updateStatistics();
        setVisible(true);
    }

    // Apply dark theme to JTable
    private void applyDarkThemeToTable(JTable table) {
        table.setBackground(backgroundColor);
        table.setForeground(textColor);
        table.setGridColor(borderColor);
        table.getTableHeader().setBackground(buttonColor);
        table.getTableHeader().setForeground(textColor);
    }

    // Load images from resource folder
    protected ImageIcon createImageIcon(String path) {
        /*URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }*/
        return null;
    }

    private JButton createButton(String text, ImageIcon icon) {
        JButton button = new JButton(text);
       /* if (icon != null) {
            button.setIcon(icon);
        }*/
        button.setHorizontalTextPosition(SwingConstants.RIGHT);
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setFocusPainted(false);
        button.setMargin(new Insets(5, 10, 5, 10));
        button.setFont(new Font("Arial", Font.PLAIN, 10)); // Smaller Button Font
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBackground(buttonColor);
        button.setForeground(textColor);

        return button;
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    // Update the Webcam Display
    public void updateWebcam(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            ImageIcon icon = new ImageIcon(image);
            webcamLabel.setIcon(icon);
            webcamLabel.repaint();
        });
    }

    public void updateScreen(BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            ImageIcon icon = new ImageIcon(image);
            screenLabel.setIcon(icon);
            screenLabel.repaint();
        });
    }

    // Get Microphone list
    private String[] getMicrophoneList() {
        List<String> microphones = new ArrayList<>();
        Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
        for (Mixer.Info mixerInfo : mixerInfos) {
            if (mixerInfo.getDescription().contains("Capture")) {
                microphones.add(mixerInfo.getName());
            }
        }
        return microphones.toArray(new String[0]);
    }

    // Get Webcam list
    private String[] getWebcamList() {
        List<String> webcams = new ArrayList<>();
        for (Webcam webcam : Webcam.getWebcams()) {
            webcams.add(webcam.getName());
        }
        return webcams.toArray(new String[0]);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }

    public void addClient(String ipAddress, String connectionStatus, String openWindow, String status) {
        SwingUtilities.invokeLater(() -> {
            Object[] rowData = {ipAddress, connectionStatus, openWindow, status};
            tableModel.addRow(rowData);
            clientList.add(new ClientInfo(ipAddress, connectionStatus, openWindow, status));
            updateStatistics();
        });
    }

    public void updateClientStatus(String ipAddress, String status) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < clientList.size(); i++) {
                if (clientList.get(i).getIpAddress().equals(ipAddress)) {
                    clientList.get(i).setStatus(status);
                    tableModel.setValueAt(status, i, 3);
                    break;
                }
            }
            updateStatistics();
        });
    }

    public void removeClient(String ipAddress) {
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < clientList.size(); i++) {
                if (clientList.get(i).getIpAddress().equals(ipAddress)) {
                    tableModel.removeRow(i);
                    clientList.remove(i);
                    break;
                }
            }
            updateStatistics();
        });
    }

    public void removeAllClients() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            clientList.clear();
            updateStatistics();
        });
    }

    private void updateStatistics() {
        int onlineCount = 0;
        int offlineCount = 0;
        for (ClientInfo client : clientList) {
            if (client.getStatus().equalsIgnoreCase("Online")) {
                onlineCount++;
            } else {
                offlineCount++;
            }
        }
        onlineCountLabel.setText("В сети: " + onlineCount);
        offlineCountLabel.setText(" | Не в сети: " + offlineCount);
        totalCountLabel.setText(" | Всего: " + clientList.size());
    }

    private static class ClientInfo {
        private String ipAddress;
        private String connectionStatus;
        private String openWindow;
        private String status;

        public ClientInfo(String ipAddress, String connectionStatus, String openWindow, String status) {
            this.ipAddress = ipAddress;
            this.connectionStatus = connectionStatus;
            this.openWindow = openWindow;
            this.status = status;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public String getConnectionStatus() {
            return connectionStatus;
        }

        public String getOpenWindow() {
            return openWindow;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}