package com.ephesoft.dcma.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

/**
 * This class will be called when the application starts to ensure
 * WebSocket endpoints are registered
 */
@WebListener
public class WebSocketConfigurator implements ServletContextListener {    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== WebSocketConfigurator: Initialization WebSocket ===");
        // Get container of WebSocket from ServletContext
        ServerContainer serverContainer = (ServerContainer) sce.getServletContext()
                .getAttribute("javax.websocket.server.ServerContainer");
        
        if (serverContainer == null) {
            System.err.println("Unable to get ServerContainer. WebSocket will not work!");
            return;
        }
        
        try {
            // Manually register the endpoint if needed
            // Not necessary when using @ServerEndpoint, but make sure the endpoint is always registered
            ServerEndpointConfig config = ServerEndpointConfig.Builder
                    .create(WebSocketEndpoint.class, "/notification-ws")
                    .build();
            
            serverContainer.addEndpoint(config);

            // Or you can simply register the annotated endpoint
            // serverContainer.addEndpoint(WebSocketEndpoint.class);
            
            System.out.println("=== WebSocket endpoint has been initialized successfully ===");
            
            // Start the recurring message service
            MessageSender.getInstance();
            System.out.println("=== Start the recurring message service ===");
            
        } catch (Exception e) {
            System.err.println("Error initializing WebSocket endpoint: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== WebSocketConfigurator: Destroy WebSocket ===");
        
        // Stop recurring message service
        try {
            MessageSender.getInstance().shutdown();
            System.out.println("=== The recurring message service has been stopped. ===");
        } catch (Exception e) {
            System.err.println("Error stopping MessageSender: " + e.getMessage());
            e.printStackTrace();
        }
    }
}