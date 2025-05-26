package com.ephesoft.dcma.core;

/**
 * Interface callback for WebSocket events
 */
public interface WebSocketCallback {

    /**
     * Called when a message is received from the server
     * @param message The message content received
     */
    void onMessage(String message);

    /**
     * Called when a WebSocket connection is established
     */
    void onOpen();

    /**
     * Called when a WebSocket connection is closed
     * @param code The code for closing the connection
     * @param reason The reason for closing the connection
     */
    void onClose(int code, String reason);

    /**
     * Called when an error occurs with the WebSocket connection
     */
    void onError();
}
