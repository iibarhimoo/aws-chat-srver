package com.chat.model;

public class Message {
    public enum Type {
        PUBLIC, PRIVATE, SYSTEM, JOIN_ROOM, LEAVE_ROOM
    }

    private Type type;
    private String sender;
    private String content;
    private String recipient; // For private messages
    private String room;      // For room-specific messages

    // Constructors
    public Message(Type type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
    }

    // Getters
    public Type getType() { return type; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getRecipient() { return recipient; }
    public String getRoom() { return room; }
}
