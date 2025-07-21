package com.chat.net;

import com.chat.model.Message;
import java.io.*;
import java.net.Socket;

public class ClientNetworkManager {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    public void connect(String serverIp, int port) throws IOException {
        socket = new Socket(serverIp, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    listener.onMessageReceived(line);
                }
            } catch (IOException e) {
                listener.onMessageReceived("Connection lost");
            }
        }).start();
    }

    public void send(String message) {
        out.println(message);
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    public void disconnect() throws IOException {
        socket.close();
    }
}
