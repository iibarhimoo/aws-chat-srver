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

        // Chat display area
        chatArea = new JTextArea(20, 40);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Emoji panel
        JPanel emojiPanel = new JPanel(new FlowLayout());
        String[] emojis = {"😀", "😂", "❤️", "👍"};
        for (String emoji : emojis) {
            JButton emojiButton = new JButton(emoji);
            emojiButton.addActionListener(e -> inputField.setText(inputField.getText() + emoji));
            emojiPanel.add(emojiButton);
        }
        add(emojiPanel, BorderLayout.NORTH);

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField(40);
        inputField.addActionListener(e -> sendMessage(messageSender));
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(new JButton("Send") {{
            addActionListener(e -> sendMessage(messageSender));
        }}, BorderLayout.EAST);

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
    }

    private void sendMessage(MessageSender sender) {
        String text = inputField.getText();
        if (!text.isEmpty()) {
            sender.send(formatMessage(text));
            inputField.setText("");
        }
    }

    private String formatMessage(String text) {
        return "[" + getTimestamp() + "] " + text;
    }

    private String getTimestamp() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public void appendMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userList.setModel(new DefaultComboBoxModel<>(users));
        });
    }
}
