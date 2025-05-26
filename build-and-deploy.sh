#!/usr/bin/env bash

echo "===== Building WebSocket Application ====="

# Go to the project directory
cd "$(dirname "$0")"

# Ask for Tomcat installation path
read -p "Enter the path to Tomcat installation (e.g., /opt/tomcat): " CATALINA_HOME

# Validate CATALINA_HOME
if [ ! -d "$CATALINA_HOME/webapps" ]; then
    echo "Error: Invalid Tomcat installation path. Cannot find webapps directory."
    exit 1
fi

echo
echo "===== Building Server Module ====="
cd ../websocket-server-tomcat
mvn clean install
if [ $? -ne 0 ]; then
    echo "Error building server module"
    exit 1
fi

echo
echo "===== Building Core Module ====="
cd websocket-gwt-core
mvn clean install
if [ $? -ne 0 ]; then
    echo "Error building core module"
    exit 1
fi

echo
echo "===== Building Client Module ====="
cd ../websocket-gwt-client
mvn clean install
if [ $? -ne 0 ]; then
    echo "Error building client module"
    exit 1
fi

echo
echo "===== Building Client Manager Module ====="
cd ../websocket-gwt-client-manager
mvn clean install
if [ $? -ne 0 ]; then
    echo "Error building client manager module"
    exit 1
fi


echo
echo "===== Deploying to Tomcat ====="

# Copy WAR files to Tomcat webapps directory
cp -f target/websocket-server.war "$CATALINA_HOME/webapps/"
cd ../websocket-gwt-client
cp -f target/websocket-gwt-client.war "$CATALINA_HOME/webapps/"
cd ../websocket-gwt-client-manager
cp -f target/websocket-gwt-client-manager.war "$CATALINA_HOME/webapps/"

echo
echo "===== Deployment Complete ====="
echo "Applications are deployed to Tomcat at $CATALINA_HOME"

echo
read -p "Do you want to start Tomcat now? (y/n): " START_TOMCAT
if [ "$START_TOMCAT" = "y" ] || [ "$START_TOMCAT" = "Y" ]; then
    echo
    echo "===== Starting Tomcat ====="

    # Check if Tomcat is already running by checking for Java processes with Tomcat
    if pgrep -f "catalina" > /dev/null; then
        echo "Tomcat or another Java process appears to be running."
        read -p "Do you want to stop and restart Tomcat? (y/n): " RESTART_TOMCAT
        if [ "$RESTART_TOMCAT" = "y" ] || [ "$RESTART_TOMCAT" = "Y" ]; then
            echo "Stopping current Tomcat instance..."
            "$CATALINA_HOME/bin/shutdown.sh"
            sleep 5
            echo "Starting Tomcat..."
            "$CATALINA_HOME/bin/startup.sh"
        else
            echo "Tomcat restart cancelled."
        fi
    else
        echo "Starting Tomcat..."
        "$CATALINA_HOME/bin/startup.sh"
    fi

    echo
    echo "Tomcat is starting. Please wait a moment for the applications to deploy."
    echo
    echo "You can access the applications at:"
    echo "- http://localhost:8080/websocket-gwt-client/WebSocketClient.html"
    echo "- http://localhost:8080/websocket-gwt-client-manager/WebSocketClientManager.html"
    echo "- http://localhost:8080/websocket-server/ (Server admin page if any)"
else
    echo
    echo "To access the applications, start Tomcat manually and navigate to:"
    echo "- http://localhost:8080/websocket-gwt-client/WebSocketClient.html"
    echo "- http://localhost:8080/websocket-gwt-client-manager/WebSocketClientManager.html"
    echo "- http://localhost:8080/websocket-server/ (Server admin page if any)"
fi