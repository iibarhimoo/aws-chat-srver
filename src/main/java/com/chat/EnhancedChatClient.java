package com.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class EnhancedChatClient {
    private JFrame frame = new JFrame("Enhanced Chat");
    private JTextArea chatArea = new JTextArea(20, 40);
    private JTextField inputField = new JTextField(40);
    private JComboBox<String> roomSelector = new JComboBox<>(new String[]{"General", "Sports"});
    private PrintWriter out;
    private String username;

    public EnhancedChatClient(String serverIp) {
        initializeGUI();
        connectToServer(serverIp);
    }

    private void initializeGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        chatArea.setEditable(false);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Room:"));
        inputPanel.add(roomSelector);
        inputPanel.add(inputField);
        inputPanel.add(new JButton("Send") {{
            addActionListener(e -> sendMessage());
        }});

        roomSelector.addActionListener(e -> {
            out.println("/join " + roomSelector.getSelectedItem());
        });

        inputField.addActionListener(e -> sendMessage());
        frame.add(inputPanel, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

    private void connectToServer(String serverIp) {
        try {
            Socket socket = new Socket(serverIp, 8080);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Authentication
            username = JOptionPane.showInputDialog(frame, "Username:");
            String password = JOptionPane.showInputDialog(frame, "Password:");
            out.println(username);
            out.println(password);

            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        String finalLine = line;
                        SwingUtilities.invokeLater(() -> chatArea.append(finalLine + "\n"));
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> 
                        chatArea.append("Disconnected from server\n"));
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Connection failed: " + e.getMessage());
        }
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            if (text.startsWith("/pm ")) {
                out.println(text);
            } else {
                out.println(text);
            }
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        String serverIp = JOptionPane.showInputDialog("Enter server IP:", "localhost");
        SwingUtilities.invokeLater(() -> new EnhancedChatClient(serverIp));
    }
}
