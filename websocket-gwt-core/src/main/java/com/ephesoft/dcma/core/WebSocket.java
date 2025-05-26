package com.ephesoft.dcma.core;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * WebSocket class - Wrapper for JavaScript WebSocket API
 * Allows use of browser WebSocket from GWT application
 */
public class WebSocket extends JavaScriptObject {
    

    public static final int CONNECTING = 0;
    public static final int OPEN = 1;
    public static final int CLOSING = 2;
    public static final int CLOSED = 3;

    // Protected constructor required by GWT
    protected WebSocket() {}
    
    /**
     * Create a new WebSocket connection to the specified URL
     */
    public static native WebSocket create(String url) /*-{
        return new $wnd.WebSocket(url);
    }-*/;
    
    /**
     * Returns the current state of the WebSocket connection
     */
    public final native int getReadyState() /*-{
        return this.readyState;
    }-*/;
    
    /**
     * Send data via WebSocket connection
     */
    public final native void send(String data) /*-{
        this.send(data);
    }-*/;
    
    /**
     * Close WebSocket connection
     */
    public final native void close() /*-{
        this.close();
    }-*/;
    
    /**
     * Register callback for message event (when receiving message from server)
     */
    public final native void setOnmessage(WebSocketCallback callback) /*-{
        this.onmessage = function(e) {
            callback.@com.ephesoft.dcma.core.WebSocketCallback::onMessage(Ljava/lang/String;)(e.data);
        };
    }-*/;
    
    /**
     * Register a callback for the open event (when the connection is established)
     */
    public final native void setOnopen(WebSocketCallback callback) /*-{
        this.onopen = function(e) {
            callback.@com.ephesoft.dcma.core.WebSocketCallback::onOpen()();
        };
    }-*/;
    
    /**
     * Register callback for close event (when connection closes)
     */
    public final native void setOnclose(WebSocketCallback callback) /*-{
        this.onclose = function(e) {
            callback.@com.ephesoft.dcma.core.WebSocketCallback::onClose(ILjava/lang/String;)(e.code, e.reason);
        };
    }-*/;
    
    /**
     * Register a callback for the error event (when an error occurs)
     */
    public final native void setOnerror(WebSocketCallback callback) /*-{
        this.onerror = function(e) {
            callback.@com.ephesoft.dcma.core.WebSocketCallback::onError()();
        };
    }-*/;
}
