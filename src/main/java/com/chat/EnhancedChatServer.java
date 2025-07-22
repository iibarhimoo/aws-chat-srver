package com.chat;

import java.io.*;
import java.net.*;
import java.util.*;

public class EnhancedChatServer {
    private static final int PORT = 8080;
    private static Map<String, PrintWriter> clients = new HashMap<>();
    private static Map<String, String> userCredentials = Map.of(
        "Yahya", "12345",
        "Ibrahim", "12345",
        "Hasan", "12345",
        "Faisal", "12345",
        "Safwan", "12345",
        "Nouruldeen", "12345",
        "Saud", "12345"
    );
    private static Map<String, Set<PrintWriter>> rooms = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // Initialize rooms
        createRoom("General");
        createRoom("Group(4)");
        createRoom("NetworkProgramming");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server running on port " + PORT);
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        }
    }

    private static synchronized void createRoom(String roomName) {
        rooms.putIfAbsent(roomName, new HashSet<>());
        broadcastRoomList();
    }

    private static synchronized void broadcastRoomList() {
        String roomList = "ROOM_LIST:" + String.join(",", rooms.keySet());
        clients.values().forEach(writer -> writer.println(roomList));
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private String currentRoom = "General";

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Authentication
                if (!authenticate()) {
                    socket.close();
                    return;
                }

                synchronized (clients) {
                    clients.put(username, out);
                }

                // Send initial data
                sendRoomList(out);
                joinRoom(currentRoom);
                broadcastSystemMessage(username + " joined " + currentRoom);

                // Message handling
                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("/join ")) {
                        handleRoomChange(input.substring(6));
                    } 
                    else if (input.startsWith("/create ")) {
                        handleRoomCreation(input.substring(8));
                    }
                    else if (input.startsWith("/pm ")) {
                        handlePrivateMessage(input);
                    }
                    else {
                        broadcastRoomMessage(currentRoom, username + ": " + input);
                    }
                }
            } catch (IOException e) {
                System.out.println(username + " disconnected: " + e.getMessage());
            } finally {
                disconnectClient();
            }
        }

        private boolean authenticate() throws IOException {
            int attempts = 0;
            while (attempts < 3) {
                out.println("SUBMIT_USERNAME");
                username = in.readLine();
                out.println("SUBMIT_PASSWORD");
                String password = in.readLine();

                if (userCredentials.getOrDefault(username, "").equals(password)) {
                    out.println("AUTH_SUCCESS");
                    return true;
                }
                out.println("AUTH_FAIL");
                attempts++;
            }
            return false;
        }

        private void handleRoomChange(String roomName) {
            if (rooms.containsKey(roomName)) {
                leaveRoom(currentRoom);
                joinRoom(roomName);
                currentRoom = roomName;
            } else {
                out.println("SERVER: Room doesn't exist");
            }
        }

        private void handleRoomCreation(String roomName) {
            if (!rooms.containsKey(roomName)) {
                createRoom(roomName);
                out.println("SERVER: Created room " + roomName);
            } else {
                out.println("SERVER: Room already exists");
            }
        }

        private void handlePrivateMessage(String input) {
            String[] parts = input.split(" ", 3);
            if (parts.length == 3) {
                sendPrivateMessage(username, parts[1], parts[2]);
            }
        }

        private void joinRoom(String room) {
            rooms.get(room).add(out);
            out.println("SERVER: Joined " + room);
            broadcastRoomMessage(room, "SERVER: " + username + " joined the room");
        }

        private void leaveRoom(String room) {
            if (rooms.containsKey(room)) {
                rooms.get(room).remove(out);
                broadcastRoomMessage(room, "SERVER: " + username + " left the room");
            }
        }

        private void disconnectClient() {
            if (username != null) {
                leaveRoom(currentRoom);
                clients.remove(username);
                broadcastSystemMessage(username + " left the server");
            }
            try { socket.close(); } catch (IOException e) {}
        }
    }

    private static void sendPrivateMessage(String sender, String recipient, String message) {
        PrintWriter recipientOut = clients.get(recipient);
        if (recipientOut != null) {
            recipientOut.println("[PM from " + sender + "]: " + message);
            clients.get(sender).println("[PM to " + recipient + "]: " + message);
        } else {
            clients.get(sender).println("SERVER: User '" + recipient + "' not found");
        }
    }

    private static void broadcastRoomMessage(String room, String message) {
        if (rooms.containsKey(room)) {
            rooms.get(room).forEach(writer -> writer.println("[" + room + "] " + message));
        }
    }

    private static void broadcastSystemMessage(String message) {
        clients.values().forEach(writer -> writer.println("SERVER: " + message));
    }

    private static void sendRoomList(PrintWriter writer) {
        writer.println("ROOM_LIST:" + String.join(",", rooms.keySet()));
    }
}
