package com.ephesoft.dcma.server;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.json.Json;
import javax.websocket.Session;

/**
 * The SocketHandler class uses the Singleton pattern to manage WebSocket connections
 */
public class SocketHandler {
    
    private static final List<Session> sessions = new CopyOnWriteArrayList<>();
    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    private static final Map<Session, String> sessionUsers = new ConcurrentHashMap<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Singleton pattern for easy access from anywhere
    private static final SocketHandler INSTANCE = new SocketHandler();
    
    private SocketHandler() {
        // Private constructor to prevent creating new instance
    }
    
    public static SocketHandler getInstance() {
        return INSTANCE;
    }
    // Manage sessions
    public void addSession(Session session) {
        sessions.add(session);
        System.out.println("New session added: " + session.getId() + ", total number of connections: " + sessions.size());
    }
    
    public void removeSession(Session session) {
        sessions.remove(session);
        // Xóa tham chiếu userName nếu có
        String userName = sessionUsers.remove(session);
        if (userName != null) {
            userSessions.remove(userName);
            System.out.println("Removed user " + userName + " from list");
        }
        System.out.println("Session deleted: " + session.getId() + ", total number of remaining connections: " + sessions.size());
    }
    
    // Register username with session
    public void registerUserName(Session session, String userName) {
        // Delete old username if it already exists
        String oldUserName = sessionUsers.get(session);
        if (oldUserName != null) {
            userSessions.remove(oldUserName);
        }

        // Check if name already exists, delete old link
        Session oldSession = userSessions.get(userName);
        if (oldSession != null) {
            sessionUsers.remove(oldSession);
        }

        // Register new name
        userSessions.put(userName, session);
        sessionUsers.put(session, userName);
        System.out.println("Đã đăng ký user: " + userName + " với session: " + session.getId());
    }

    // Get user session by name
    public Session getSessionByUserName(String userName) {
        return userSessions.get(userName);
    }

    // Get username from session
    public String getUserNameFromSession(Session session) {
        return sessionUsers.getOrDefault(session, session.getId());
    }
    // Process incoming messages
    public void handleMessage(String message, Session session) {
        System.out.println("Message from " + session.getId() + ": " + message);

        String response = Json.createObjectBuilder()
            .add("type", "message")
            .add("from", "server")
            .add("content", message)
            .add("timestamp", System.currentTimeMillis())
            .add("formattedTime", LocalDateTime.now().format(formatter))
            .build().toString();
        
        broadcastMessage(response);
    }

    // Send message to a specific session
    public void sendToSession(Session session, String message) {
        try {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
                System.out.println("Message sent to session " + session.getId());
            }
        } catch (IOException e) {
            System.err.println("Error sending message to session " + session.getId());
            e.printStackTrace();
        }
    }
    // Broadcast message to all clients
    public void broadcastMessage(String message) {
        System.out.println("Send message to all clients: " + message);
        for (Session session : sessions) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                System.err.println("Error sending message to session " + session.getId());
                e.printStackTrace();
            }
        }
    }

    // Send a message to a specific user (by name)
    public boolean sendToUser(String userName, String message) {
        Session targetSession = userSessions.get(userName);
        if (targetSession != null && targetSession.isOpen()) {
            try {
                targetSession.getBasicRemote().sendText(message);
                System.out.println("Message sent to user: " + userName);
                return true;
            } catch (IOException e) {
                System.err.println("Error sending message to user " + userName);
                e.printStackTrace();
            }
        } else {
            System.out.println("User not found: " + userName + " or session closed");
        }
        return false;
    }
    // Send server status information to all clients
    public void broadcastStatus() {
        String status = Json.createObjectBuilder()
            .add("type", "status")
            .add("connections", sessions.size())
            .add("timestamp", System.currentTimeMillis())
            .add("formattedTime", LocalDateTime.now().format(formatter))
            .build().toString();
            
        broadcastMessage(status);
    }
}