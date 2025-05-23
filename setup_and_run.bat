@echo off
echo Setting up JavaFX environment...

REM Create lib directory if it doesn't exist
if not exist "lib" mkdir lib

REM Download JavaFX SDK if not already downloaded
if not exist "lib\javafx-sdk-17.0.2" (
    echo Downloading JavaFX SDK...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_windows-x64_bin-sdk.zip' -OutFile 'javafx-sdk.zip'}"
    
    echo Extracting JavaFX SDK...
    powershell -Command "& {Expand-Archive -Path 'javafx-sdk.zip' -DestinationPath 'lib' -Force}"
    del javafx-sdk.zip
)

REM Set JavaFX path
set JAVAFX_PATH=lib\javafx-sdk-17.0.2\lib

REM Compile the project
echo Compiling project...
mvn clean compile

REM Run the application with JavaFX modules
echo Running application...
java --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -cp target/classes com.lms.Main

pause 