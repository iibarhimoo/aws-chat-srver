package com.chat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class EnhancedChatClient extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JComboBox<String> userList;
    private PrintWriter out;
    private String username;

    public EnhancedChatClient() {
        initializeGUI();
        connectToServer();
    }

    private void initializeGUI() {
        setTitle("Chat Client");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Chat display
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());
        inputPanel.add(inputField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        inputPanel.add(sendButton, BorderLayout.EAST);

        // User list
        userList = new JComboBox<>();
        userList.addActionListener(e -> {
            if (userList.getSelectedIndex() > 0) {
                inputField.setText("/pm " + userList.getSelectedItem() + " ");
                inputField.requestFocus();
            }
        });

        add(inputPanel, BorderLayout.SOUTH);
        add(userList, BorderLayout.EAST);
        setVisible(true);
    }

    private void connectToServer() {
        boolean connected = false;
        int attemptsLeft = 3;

        while (!connected && attemptsLeft > 0) {
            String serverIp = JOptionPane.showInputDialog(this, 
                "Enter server IP (attempts left: " + attemptsLeft + "):", 
                "Server IP");

            if (serverIp == null) {
                System.exit(0);
            }

            try {
                Socket socket = new Socket(serverIp, 8080);
                out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Authentication loop
                while (true) {
                    String serverMsg = in.readLine();
                    if (serverMsg.equals("SUBMIT_USERNAME")) {
                        username = JOptionPane.showInputDialog(this, "Username:");
                        out.println(username);
                    } 
                    else if (serverMsg.equals("SUBMIT_PASSWORD")) {
                        JPasswordField passwordField = new JPasswordField();
                        int option = JOptionPane.showConfirmDialog(this, 
                            passwordField, "Password:", JOptionPane.OK_CANCEL_OPTION);
                        
                        if (option == JOptionPane.OK_OPTION) {
                            out.println(new String(passwordField.getPassword()));
                        } else {
                            out.println(""); // Send empty password
                        }
                    }
                    else if (serverMsg.equals("AUTH_SUCCESS")) {
                        connected = true;
                        startMessageListener(in);
                        break;
                    }
                    else if (serverMsg.startsWith("AUTH_FAIL")) {
                        attemptsLeft--;
                        JOptionPane.showMessageDialog(this, 
                            "Invalid credentials. Attempts left: " + attemptsLeft);
                        break;
                    }
                }

                if (!connected && attemptsLeft == 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Maximum attempts reached. Exiting.");
                    System.exit(0);
                }

            } catch (IOException e) {
                attemptsLeft--;
                JOptionPane.showMessageDialog(this, 
                    "Connection failed: " + e.getMessage() + "\nAttempts left: " + attemptsLeft);
            }
        }
    }

    private void startMessageListener(BufferedReader in) {
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    String finalMessage = message;
                    SwingUtilities.invokeLater(() -> {
                        chatArea.append(finalMessage + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    });
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    chatArea.append("Disconnected from server\n");
                    inputField.setEnabled(false);
                });
            }
        }).start();
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            out.println(text);
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new EnhancedChatClient());
    }
}
