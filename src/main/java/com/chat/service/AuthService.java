package com.chat.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private static final Map<String, String> userDatabase = new ConcurrentHashMap<>();

    static {
        // Preloaded users (in production, use database)
        userDatabase.put("Yahya", "networkProgramming");
        userDatabase.put("Ibrahim", "networkProgramming");
        userDatabase.put("Ammer" , "networkProgramming");
    }

    public static boolean authenticate(String username, String password) {
        return userDatabase.containsKey(username) && 
               userDatabase.get(username).equals(password);
    }

    public static void register(String username, String password) {
        userDatabase.put(username, password);
    }
}
