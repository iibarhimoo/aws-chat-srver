package com.chat;

import java.io.*;
import java.net.*;
import java.util.*;

public class EnhancedChatServer {
    private static final int PORT = 8080;
    private static Map<String, PrintWriter> clients = new HashMap<>();
    private static Map<String, String> userCredentials = Map.of(
        "Yahay", "networkProgramming",
        "Ibrahim", "networkProgramming"
        "Hasan", "networkProgramming"
        "Faisal", "networkProgramming"
        "Safwan", "networkProgramming"
        "Nouruldeen", "networkProgramming"
        "Saud", "networkProgramming"
    );
    private static Map<String, Set<PrintWriter>> rooms = new HashMap<>();

    public static void main(String[] args) throws IOException {
        rooms.put("General", new HashSet<>());
        rooms.put("Group(4)", new HashSet<>());
        rooms.put("NetworkProgramming", new HashSet<>());
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server running on port " + PORT);
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        }
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
                while (true) {
                    out.println("SUBMIT_USERNAME");
                    username = in.readLine();
                    out.println("SUBMIT_PASSWORD");
                    String password = in.readLine();

                    if (userCredentials.getOrDefault(username, "").equals(password)) {
                        out.println("AUTH_SUCCESS");
                        break;
                    } else {
                        while(false){
                        out.println("AUTH_FAIL");
                        return;
                        userCredentials.getOrDefault(username, "").equals(password)
                    }
                }

                synchronized (clients) {
                    clients.put(username, out);
                }
                joinRoom(currentRoom);

                broadcastSystemMessage(username + " joined " + currentRoom);

                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("/join ")) {
                        String newRoom = input.substring(6);
                        leaveRoom(currentRoom);
                        joinRoom(newRoom);
                        currentRoom = newRoom;
                    } else if (input.startsWith("/pm ")) {
                        String[] parts = input.split(" ", 3);
                        if (parts.length == 3) {
                            sendPrivateMessage(username, parts[1], parts[2]);
                        }
                    } else {
                        broadcastRoomMessage(currentRoom, username + ": " + input);
                    }
                }
            } catch (IOException e) {
                System.out.println(username + " disconnected: " + e.getMessage());
            } finally {
                if (username != null) {
                    leaveRoom(currentRoom);
                    clients.remove(username);
                    broadcastSystemMessage(username + " left the server");
                }
                try { socket.close(); } catch (IOException e) {}
            }
        }

        private void joinRoom(String room) {
            rooms.computeIfAbsent(room, k -> new HashSet<>()).add(out);
            out.println("SERVER: Joined " + room);
        }

        private void leaveRoom(String room) {
            if (rooms.containsKey(room)) {
                rooms.get(room).remove(out);
                broadcastRoomMessage(room, "SERVER: " + username + " left");
            }
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
}
