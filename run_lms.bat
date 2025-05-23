@echo off
cd /d "%~dp0"

REM Set environment variables
set JAVA_HOME=C:\Program Files\Java\jdk-22
set PATH=%JAVA_HOME%\bin;%~dp0tools\apache-maven-3.9.6\bin;%PATH%
set JAVAFX_PATH=%~dp0lib\javafx-sdk-17.0.2\lib

REM Check if JavaFX SDK exists
if not exist "%JAVAFX_PATH%" (
    echo JavaFX SDK not found. Please run setup_environment.bat first.
    pause
    exit /b 1
)

REM Clean and compile
call mvn clean compile

REM Run with explicit JavaFX modules and PostgreSQL driver
java --module-path "%JAVAFX_PATH%" ^
     --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base ^
     --add-opens java.sql/java.sql=ALL-UNNAMED ^
     -cp "target/classes;target/dependency/*" com.lms.Main

pause 