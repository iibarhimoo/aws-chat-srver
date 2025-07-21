package com.chat.gui;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel {
    // Define MessageSender as a functional interface
    @FunctionalInterface
    public interface MessageSender {
        void send(String message);
    }

    private final JTextArea chatArea;
    private final JTextField inputField;
    private final JComboBox<String> userList;

    public ChatPanel(MessageSender messageSender) {
        setLayout(new BorderLayout());
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage(messageSender));
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(new JButton("Send") {{
            addActionListener(e -> sendMessage(messageSender));
        }}, BorderLayout.EAST);

        userList = new JComboBox<>();
        userList.addActionListener(e -> {
            if (userList.getSelectedIndex() > 0) {
                inputField.setText("/pm " + userList.getSelectedItem() + " ");
                inputField.requestFocus();
            }
        });

        add(inputPanel, BorderLayout.SOUTH);
        add(userList, BorderLayout.EAST);
    }

    private void sendMessage(MessageSender sender) {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            sender.send(text);
            inputField.setText("");
        }
    }

    public void appendMessage(String message) {
        chatArea.append(message + "\n");
    }

    public void updateUserList(String[] users) {
        userList.setModel(new DefaultComboBoxModel<>(users));
    }
}
