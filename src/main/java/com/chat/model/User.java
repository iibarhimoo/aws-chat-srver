package com.chat.model;

import java.io.PrintWriter;

public class User {
    private String username;
    private String currentRoom;
    private transient PrintWriter writer; // Not serialized for network

    public User(String username, PrintWriter writer) {
        this.username = username;
        this.writer = writer;
        this.currentRoom = "General";
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public String getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(String room) { this.currentRoom = room; }
    public PrintWriter getWriter() { return writer; }
}
