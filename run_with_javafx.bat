@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-22
set PATH=%JAVA_HOME%\bin;%PATH%

REM Clean and compile
call mvn clean compile

REM Run with explicit JavaFX modules
java --module-path "%JAVA_HOME%\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -cp target/classes com.lms.Main

pause 