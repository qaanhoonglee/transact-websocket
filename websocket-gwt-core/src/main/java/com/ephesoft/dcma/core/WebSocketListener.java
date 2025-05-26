package com.ephesoft.dcma.core;

/**
 * Interface for listeners who want to receive notifications from WebSocketManager

 */
public interface WebSocketListener {

    /**
     * Called when the WebSocket connection is successfully established
     */
    void onConnected();

    /**
     * Called when the WebSocket connection is closed
     *
     * @param code The code for closing the connection
     * @param reason The reason for closing the connection
     */
    void onDisconnected(int code, String reason);

    /**
     * Called when a message is received from the server
     *
     * @param message The message content
     */
    void onMessageReceived(String message);

    /**
     * Called when an error occurs with the WebSocket connection
     */
    void onError();
}