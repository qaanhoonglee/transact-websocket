package com.ephesoft.dcma;

import com.ephesoft.dcma.core.WebSocketManager;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * Entry point cho ứng dụng GWT WebSocket Client Business
 * Lớp này khởi tạo UI và hiển thị BusinessView
 */
public class WebSocketClientBusinessApp implements EntryPoint {

    /**
     * Entry point method - được gọi khi ứng dụng GWT khởi động
     */
    public void onModuleLoad() {
        // Khởi tạo WebSocketManager và đăng ký tên người dùng mặc định
        WebSocketManager webSocketManager = WebSocketManager.getInstance();
        webSocketManager.setCurrentUserName("Business");

        // Kết nối tới WebSocket server
        webSocketManager.connect();

        // Tạo và hiển thị view quản lý
        BusinessView managerView = new BusinessView();
        RootLayoutPanel.get().add(managerView);
    }
}
