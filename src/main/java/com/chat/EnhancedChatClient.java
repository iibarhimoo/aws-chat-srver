package com.chat.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class EnhancedChatClient extends JFrame {
    private JTextArea chatArea;
    private JTextField inputField;
    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private PrintWriter out;
    private String username;

    public EnhancedChatClient() {
        initializeGUI();
        connectToServer();
    }

    private void initializeGUI() {
        setTitle("Chat Client");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Chat display
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        add(chatScroll, BorderLayout.CENTER);

        // Room panel
        JPanel roomPanel = new JPanel(new BorderLayout());
        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomPanel.add(new JLabel("Rooms:"), BorderLayout.NORTH);
        roomPanel.add(new JScrollPane(roomList), BorderLayout.CENTER);

        // User panel
        JPanel userPanel = new JPanel(new BorderLayout());
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userPanel.add(new JLabel("Users:"), BorderLayout.NORTH);
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

        // Combined side panel
        JPanel sidePanel = new JPanel(new GridLayout(2, 1));
        sidePanel.add(roomPanel);
        sidePanel.add(userPanel);
        add(sidePanel, BorderLayout.EAST);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());
        
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());
        
        JButton createRoomButton = new JButton("Create Room");
        createRoomButton.addActionListener(e -> createRoom());
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(createRoomButton);
        buttonPanel.add(sendButton);
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(inputPanel, BorderLayout.SOUTH);

        // Room list double-click handler
        roomList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    joinSelectedRoom();
                }
            }
        });

        setVisible(true);
    }

    private void connectToServer() {
        String serverIp = JOptionPane.showInputDialog(this, "Enter server IP:", "Server IP");
        if (serverIp == null) {
            System.exit(0);
        }

        try {
            Socket socket = new Socket(serverIp, 8080);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Authentication
            authenticate(in);

            // Start message listener
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        handleServerMessage(message);
                    }
                } catch (IOException e) {
                    appendToChat("Disconnected from server");
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Connection failed: " + e.getMessage());
            System.exit(1);
        }
    }

    private void authenticate(BufferedReader in) throws IOException {
        boolean authenticated = false;
        int attempts = 0;
        
        while (!authenticated && attempts < 3) {
            String serverResponse = in.readLine();
            if (serverResponse.equals("SUBMIT_USERNAME")) {
                username = JOptionPane.showInputDialog(this, "Username:");
                out.println(username);
            } 
            else if (serverResponse.equals("SUBMIT_PASSWORD")) {
                JPasswordField passwordField = new JPasswordField();
                int option = JOptionPane.showConfirmDialog(
                    this, passwordField, "Password:", JOptionPane.OK_CANCEL_OPTION);
                
                if (option == JOptionPane.OK_OPTION) {
                    out.println(new String(passwordField.getPassword()));
                } else {
                    out.println("");
                }
            }
            else if (serverResponse.equals("AUTH_SUCCESS")) {
                authenticated = true;
            }
            else if (serverResponse.equals("AUTH_FAIL")) {
                attempts++;
                JOptionPane.showMessageDialog(this, 
                    "Invalid credentials. Attempts left: " + (3 - attempts));
            }
        }
        
        if (!authenticated) {
            JOptionPane.showMessageDialog(this, "Authentication failed");
            System.exit(0);
        }
    }

    private void handleServerMessage(String message) {
        if (message.startsWith("ROOM_LIST:")) {
            updateRoomList(message.substring(10));
        }
        else if (message.startsWith("USER_LIST:")) {
            updateUserList(message.substring(10));
        }
        else {
            appendToChat(message);
        }
    }

    private void updateRoomList(String rooms) {
        SwingUtilities.invokeLater(() -> {
            roomListModel.clear();
            Arrays.stream(rooms.split(","))
                  .forEach(roomListModel::addElement);
        });
    }

    private void updateUserList(String users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            Arrays.stream(users.split(","))
                  .forEach(userListModel::addElement);
        });
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            out.println(text);
            inputField.setText("");
        }
    }

    private void createRoom() {
        String roomName = JOptionPane.showInputDialog(this, "Enter new room name:");
        if (roomName != null && !roomName.trim().isEmpty()) {
            out.println("/create " + roomName.trim());
        }
    }

    private void joinSelectedRoom() {
        String selectedRoom = roomList.getSelectedValue();
        if (selectedRoom != null) {
            out.println("/join " + selectedRoom);
        }
    }

    private void appendToChat(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EnhancedChatClient::new);
    }
}
