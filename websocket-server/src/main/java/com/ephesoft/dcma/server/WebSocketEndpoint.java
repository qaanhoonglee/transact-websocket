package com.ephesoft.dcma.server;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.StringReader;

/**
 * WebSocket Endpoint to handle connection and messages
 * Use @ServerEndpoint annotation to register WebSocket endpoint
 */
@ServerEndpoint(value="/notification-ws")
public class WebSocketEndpoint {
    
    private final SocketHandler handler = SocketHandler.getInstance();
    
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("New connection established: " + session.getId());
        handler.addSession(session);
        
        // Gửi tin nhắn chào mừng
        String welcome = Json.createObjectBuilder()
            .add("type", "welcome")
            .add("message", "Connection to WebSocket server successful")
            .add("sessionId", session.getId())
            .add("timestamp", System.currentTimeMillis())
            .build().toString();
        
        handler.sendToSession(session, welcome);
    }
      @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Nhận tin nhắn từ " + session.getId() + ": " + message);
        
        try {
            JsonReader jsonReader = Json.createReader(new StringReader(message));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();
            
            String type = jsonObject.getString("type", "");

            // Process by message type
            switch (type) {
                case "register":
                    String userName = jsonObject.getString("userName", "");
                    if (!userName.isEmpty()) {
                        // Register username with current session
                        handler.registerUserName(session, userName);

                        // Send successful registration confirmation message
                        String registerResponse = Json.createObjectBuilder()
                            .add("type", "register-confirm")
                            .add("userName", userName)
                            .add("timestamp", System.currentTimeMillis())
                            .build().toString();
                        
                        handler.sendToSession(session, registerResponse);

                        // Notify other clients
                        String userJoined = Json.createObjectBuilder()
                            .add("type", "system")
                            .add("message", "User " + userName + " has joined")
                            .add("timestamp", System.currentTimeMillis())
                            .build().toString();
                        
                        handler.broadcastMessage(userJoined);
                    }
                    break;
                    
                case "chat":
                    String content = jsonObject.getString("content", "");
                    String sender = handler.getUserNameFromSession(session);

                    // Check if sending to specific user
                    if (jsonObject.containsKey("recipient")) {
                        String recipient = jsonObject.getString("recipient", "");
                        if (!recipient.isEmpty()) {
                            String privateMsg = Json.createObjectBuilder()
                                .add("type", "private-chat")
                                .add("sender", sender)
                                .add("content", content)
                                .add("timestamp", System.currentTimeMillis())
                                .build().toString();

                            // Send message to recipient
                            boolean sent = handler.sendToUser(recipient, privateMsg);

                            // Send a copy of the message to the sender
                            if (sent) {
                                String confirmMsg = Json.createObjectBuilder()
                                    .add("type", "private-chat-sent")
                                    .add("recipient", recipient)
                                    .add("content", content)
                                    .add("timestamp", System.currentTimeMillis())
                                    .build().toString();
                                
                                handler.sendToSession(session, confirmMsg);
                            } else {
                                // Notify if unable to send
                                String errorMsg = Json.createObjectBuilder()
                                    .add("type", "error")
                                    .add("message", "Unable to send message to " + recipient)
                                    .add("timestamp", System.currentTimeMillis())
                                    .build().toString();
                                
                                handler.sendToSession(session, errorMsg);
                            }
                            return;
                        }
                    }

                    // If no specific recipient or empty recipient, send to all
                    String response = Json.createObjectBuilder()
                        .add("type", "chat")
                        .add("sender", sender)
                        .add("content", content)
                        .add("timestamp", System.currentTimeMillis())
                        .build().toString();
                    
                    // Send to all clients
                    handler.broadcastMessage(response);
                    break;
                    
                case "ping":
                    String pong = Json.createObjectBuilder()
                        .add("type", "pong")
                        .add("timestamp", System.currentTimeMillis())
                        .build().toString();

                    // Send only to calling client
                    handler.sendToSession(session, pong);
                    break;
                    
                default:
                    // If not JSON format or no type field
                    handler.handleMessage(message, session);
                    break;
            }
        } catch (Exception e) {
            // If not JSON format, process as normal message
            handler.handleMessage(message, session);
        }
    }
      @OnClose
    public void onClose(Session session) {
        System.out.println("Closed connection: " + session.getId());

          // Get username before deleting session
        String userName = handler.getUserNameFromSession(session);
        
        handler.removeSession(session);

          // Notify other clients
        String notification = Json.createObjectBuilder()
            .add("type", "system")
            .add("message", "User " + userName + " has disconnected")
            .add("timestamp", System.currentTimeMillis())
            .build().toString();
            
        handler.broadcastMessage(notification);
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("An error occurred in the session. " + session.getId());
        throwable.printStackTrace();
    }
}