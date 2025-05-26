@echo off
echo ===== Building WebSocket Application =====

REM Go to the project directory
cd /d %~dp0

REM Ask for Tomcat installation path
set /p CATALINA_HOME="Enter the path to Tomcat installation (e.g., C:\jdks\apache-tomcat-9.0.69): "

REM Validate CATALINA_HOME
if not exist "%CATALINA_HOME%\webapps" (
    echo Error: Invalid Tomcat installation path. Cannot find webapps directory.
    exit /b 1
)

echo.
echo ===== Building Core Module =====
cd websocket-gwt-core
call mvn clean install
if %errorlevel% neq 0 (
    echo Error building core module
    exit /b %errorlevel%
)

echo.
echo ===== Building Server Module =====
cd ..\websocket-server
call mvn clean install
if %errorlevel% neq 0 (
    echo Error building server module
    exit /b %errorlevel%
)

echo.
echo ===== Building Client Module =====
cd ..\websocket-gwt-client
call mvn clean install
if %errorlevel% neq 0 (
    echo Error building client module
    exit /b %errorlevel%
)

echo.
echo ===== Building Client Manager Module =====
cd ..\websocket-gwt-client-manager
call mvn clean install
if %errorlevel% neq 0 (
    echo Error building client manager module
    exit /b %errorlevel%
)

echo.
echo ===== Building Client Business Module =====
cd ..\websocket-gwt-client-business
call mvn clean install
if %errorlevel% neq 0 (
    echo Error building client business module
    exit /b %errorlevel%
)
echo.
echo ===== Deploying to Tomcat =====

REM Copy WAR files to Tomcat webapps directory
cd ..\websocket-server
copy /Y target\websocket-server.war "%CATALINA_HOME%\webapps\"
cd ..\websocket-gwt-core
copy /Y target\websocket-gwt-core-1.0-SNAPSHOT.jar "%CATALINA_HOME%\webapps\"
cd ..\websocket-gwt-client
copy /Y target\websocket-gwt-client.war "%CATALINA_HOME%\webapps\"
cd ..\websocket-gwt-client-manager
copy /Y target\websocket-gwt-client-manager.war "%CATALINA_HOME%\webapps\"
cd ..\websocket-gwt-client-business
copy /Y target\websocket-gwt-client-business.war "%CATALINA_HOME%\webapps\"

echo.
echo ===== Deployment Complete =====
echo Applications are deployed to Tomcat at %CATALINA_HOME%

echo.
set /p START_TOMCAT="Do you want to start Tomcat now? (y/n): "
if /i "%START_TOMCAT%"=="y" (
    echo.
    echo ===== Starting Tomcat =====

    REM Check if Tomcat is already running by checking if Java process with Tomcat is active
    tasklist /FI "IMAGENAME eq java.exe" | find "java.exe" > nul
    if %errorlevel%==0 (
        echo Tomcat or another Java process appears to be running.
        set /p RESTART_TOMCAT="Do you want to stop and restart Tomcat? (y/n): "
        if /i "%RESTART_TOMCAT%"=="y" (
            echo Stopping current Tomcat instance...
            call "%CATALINA_HOME%\bin\shutdown.bat"
            timeout /t 5
            echo Starting Tomcat...
            start "" "%CATALINA_HOME%\bin\startup.bat"
        ) else (
            echo Tomcat restart cancelled.
        )
    ) else (
        echo Starting Tomcat...
        start "" "%CATALINA_HOME%\bin\startup.bat"
    )

    echo.
    echo Tomcat is starting. Please wait a moment for the applications to deploy.
    echo.
    echo You can access the applications at:
    echo - http://localhost:8080/websocket-gwt-client/WebSocketClient.html
    echo - http://localhost:8080/websocket-gwt-client-manager/WebSocketClientManager.html
    echo - http://localhost:8080/websocket-gwt-client-business/WebSocketClientBusiness.html
    echo - http://localhost:8080/websocket-server/ (Server admin page if any)
) else (
    echo.
    echo To access the applications, start Tomcat manually and navigate to:
    echo - http://localhost:8080/websocket-gwt-client/WebSocketClient.html
    echo - http://localhost:8080/websocket-gwt-client-manager/WebSocketClientManager.html
    echo - http://localhost:8080/websocket-gwt-client-business/WebSocketClientBusiness.html
    echo - http://localhost:8080/websocket-server/ (Server admin page if any)
)
