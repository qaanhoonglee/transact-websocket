package com.ephesoft.dcma.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utility to send periodic messages to all connected clients
 * Example for push notification from server
 */
public class MessageSender {
    
    private final SocketHandler socketHandler;
    private final ScheduledExecutorService scheduler;
    private static final MessageSender INSTANCE = new MessageSender();
    
    private MessageSender() {
        this.socketHandler = SocketHandler.getInstance();
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        // Start sending recurring messages
        startSendingMessages();
    }
    
    public static MessageSender getInstance() {
        return INSTANCE;
    }
      private void startSendingMessages() {
        scheduler.scheduleAtFixedRate(() -> {
            String message = javax.json.Json.createObjectBuilder()
                .add("type", "server-time")
                .add("timestamp", System.currentTimeMillis())
                .add("serverTime", java.time.LocalDateTime.now().toString())
                .build().toString();
            socketHandler.broadcastMessage(message);
        }, 2, 10, TimeUnit.MINUTES);
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(25, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
