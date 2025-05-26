package com.ephesoft.dcma.core;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class that manages WebSocket connections and handles callbacks
 * This class acts as a common base class for views
 */
public class WebSocketManager implements WebSocketCallback {
    
    // WebSocket object
    private WebSocket webSocket;
    private String currentUserName = "";
    private String serverUrl = "ws://localhost:8080/websocket-server/notification-ws";
    private boolean isConnected = false;
    
    // Format time
    private DateTimeFormat timeFormat = DateTimeFormat.getFormat("HH:mm:ss");
    
    // List of listeners to notify when WebSocket events occur
    private List<WebSocketListener> listeners = new ArrayList<>();
    
    // Singleton pattern
    private static WebSocketManager instance;
    
    /**
     * Constructor
     */
    private WebSocketManager() {
        // Private constructor to ensure singleton
    }
    
    /**
     * Get instance of WebSocketManager
     */
    public static WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }
    
    /**
     * Set URL for WebSocket server
     */
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
    
    /**
     * Get current connection status
     */
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Get current username
     */
    public String getCurrentUserName() {
        return currentUserName;
    }
    
    /**
     * Set username
     */
    public void setCurrentUserName(String userName) {
        this.currentUserName = userName;
    }
    
    /**
     * Add listener to receive WebSocket event notifications
     */
    public void addListener(WebSocketListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remove listener
     */
    public void removeListener(WebSocketListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Connect to WebSocket server
     */
    public void connect() {
        if (webSocket != null) {
            if (webSocket.getReadyState() != WebSocket.CLOSED) {
                GWT.log("WebSocket is connected or connecting");
                return;
            }
            webSocket = null;
        }
        
        try {
            GWT.log("Connecting to " + serverUrl);
            webSocket = WebSocket.create(serverUrl);
            webSocket.setOnopen(this);
            webSocket.setOnmessage(this);
            webSocket.setOnclose(this);
            webSocket.setOnerror(this);
        } catch (Exception e) {
            GWT.log("WebSocket connection error: " + e.getMessage());
            notifyError();
        }
    }
    
    /**
     * Disconnect WebSocket
     */
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close();
        }
    }
    
    /**
     * Send messages via WebSocket
     */
    public void sendMessage(String message) {
        if (isConnected && webSocket != null) {
            try {
                webSocket.send(message);
                System.out.println("======== Message sent: " + message);
                GWT.log("======== Message sent: " + message);
            } catch (Exception e) {
                GWT.log("Error sending message: " + e.getMessage());
            }
        } else {
            GWT.log("Failed to send message: WebSocket not connected");
        }
    }
    
    /**
     * Send chat messages in JSON format
     */
    public void sendChatMessage(String content, String recipient) {

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"type\": \"chat\",");
        jsonBuilder.append("\"sender\": \"").append(escapeJsonString(currentUserName)).append("\",");
        jsonBuilder.append("\"content\": \"").append(escapeJsonString(content)).append("\",");

        // Add recipient if specified
        if (recipient != null && !recipient.isEmpty()) {
            jsonBuilder.append("\"recipient\": \"").append(escapeJsonString(recipient)).append("\",");
        }
        
        jsonBuilder.append("\"timestamp\": ").append(System.currentTimeMillis());
        jsonBuilder.append("}");
        
        sendMessage(jsonBuilder.toString());
    }
    
    /**
     * Register username with server
     */
    public void registerUserName(String userName) {
        if (userName == null || userName.trim().isEmpty()) {
            return;
        }
        
        currentUserName = userName;

        // Create JSON message to register username
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");
        jsonBuilder.append("\"type\": \"register\",");
        jsonBuilder.append("\"userName\": \"").append(escapeJsonString(userName)).append("\"");
        jsonBuilder.append("}");
        
        sendMessage(jsonBuilder.toString());
    }
    
    /**
     * Send ping to test connection
     */
    public void sendPing() {
        if (isConnected) {
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"type\": \"ping\",");
            jsonBuilder.append("\"timestamp\": ").append(System.currentTimeMillis());
            jsonBuilder.append("}");
            
            sendMessage(jsonBuilder.toString());
        }
    }
    
    // Implementations of WebSocketCallback interface
    
    @Override
    public void onOpen() {
        GWT.log("WebSocket connected successfully!");
        isConnected = true;

        // If username already exists, register with server
        if (currentUserName != null && !currentUserName.isEmpty()) {
            registerUserName(currentUserName);
        }

        // Notify all listeners
        for (WebSocketListener listener : listeners) {
            listener.onConnected();
        }
    }
    
    @Override
    public void onClose(int code, String reason) {
        GWT.log("WebSocket closed: " + code + " - " + reason);
        isConnected = false;

        // Notify all listeners
        for (WebSocketListener listener : listeners) {
            listener.onDisconnected(code, reason);
        }
    }
    
    @Override
    public void onMessage(String message) {
        GWT.log("Receiving WebSocket message: " + message);
        
        // Thông báo cho tất cả listeners
        for (WebSocketListener listener : listeners) {
            listener.onMessageReceived(message);
        }
    }
    
    @Override
    public void onError() {
        GWT.log("Error WebSocket");
        notifyError();
    }
    
    /**
     * Report errors to all listeners
     */
    private void notifyError() {
        for (WebSocketListener listener : listeners) {
            listener.onError();
        }
    }

    /**
     * Simple method to extract a value from a field in a JSON string
     * Note: This is a simple implementation, not a full JSON parser
     */
    public static String extractJsonField(String jsonString, String fieldName) {
        int fieldIndex = jsonString.indexOf("\"" + fieldName + "\"");
        if (fieldIndex < 0) {
            return null;
        }
        
        int colonIndex = jsonString.indexOf(":", fieldIndex);
        if (colonIndex < 0) {
            return null;
        }
        
        int valueStartIndex = jsonString.indexOf("\"", colonIndex);
        if (valueStartIndex < 0) {
            // Có thể là số hoặc boolean
            int commaIndex = jsonString.indexOf(",", colonIndex);
            int bracketIndex = jsonString.indexOf("}", colonIndex);
            int endIndex = (commaIndex > 0 && commaIndex < bracketIndex) ? commaIndex : bracketIndex;
            
            if (endIndex > 0) {
                return jsonString.substring(colonIndex + 1, endIndex).trim();
            } else {
                return null;
            }
        }
        
        int valueEndIndex = jsonString.indexOf("\"", valueStartIndex + 1);
        if (valueEndIndex < 0) {
            return null;
        }
        
        return jsonString.substring(valueStartIndex + 1, valueEndIndex);
    }

    /**
     * Escape special characters in JSON string
     */
    private String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }
        
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
