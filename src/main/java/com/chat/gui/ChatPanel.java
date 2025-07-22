package com.chat.gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatPanel extends JPanel {
    @FunctionalInterface
    public interface MessageSender {
        void send(String message);
    }

    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JComboBox<String> userList;

    public ChatPanel(MessageSender messageSender) {
        setLayout(new BorderLayout());

        // Chat display
        chatArea = new JTextArea(20, 40);
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputPanel.add(inputField, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage(messageSender));
        inputPanel.add(sendButton, BorderLayout.EAST);

        // User list
        userList = new JComboBox<>();
        userList.addActionListener(e -> {
            if (userList.getSelectedIndex() > 0) {
                inputField.setText("/pm " + userList.getSelectedItem() + " ");
            }
        });

        add(inputPanel, BorderLayout.SOUTH);
        add(userList, BorderLayout.EAST);

        // Set up enter key to send
        inputField.addActionListener(e -> sendMessage(messageSender));
    }

    private void sendMessage(MessageSender sender) {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            sender.send("[" + getTimestamp() + "] " + text);
            inputField.setText("");
        }
    }

    private String getTimestamp() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public void appendMessage(String message) {
        chatArea.append(message + "\n");
    }

    public void updateUserList(String[] users) {
        userList.setModel(new DefaultComboBoxModel<>(users));
    }
}
