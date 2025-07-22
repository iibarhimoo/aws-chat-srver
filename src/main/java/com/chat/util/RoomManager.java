package com.chat.util;

import com.chat.model.User;
import java.util.*;
import java.io.PrintWriter;

public class RoomManager {
    private static final Map<String, Set<User>> rooms = new HashMap<>();

    static {
        rooms.put("General", new HashSet<>());
        rooms.put("Group(4)", new HashSet<>());
        rooms.put("NetworkProgramming", new HashSet<>());
    }

    public static synchronized void joinRoom(User user, String roomName) {
        leaveCurrentRoom(user);
        rooms.computeIfAbsent(roomName, k -> new HashSet<>()).add(user);
        user.setCurrentRoom(roomName);
    }

    public static synchronized void leaveCurrentRoom(User user) {
        rooms.getOrDefault(user.getCurrentRoom(), Collections.emptySet())
             .remove(user);
    }

    public static synchronized void broadcast(String room, String message) {
        rooms.getOrDefault(room, Collections.emptySet())
             .forEach(user -> user.getWriter().println(message));
    }
}
