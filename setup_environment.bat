@echo off
cd /d "%~dp0"

echo Setting up development environment...

REM Create directories
if not exist "tools" mkdir tools
if not exist "lib" mkdir lib

REM Download Maven if not exists
if not exist "tools\apache-maven-3.9.6" (
    echo Downloading Maven...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip' -OutFile 'tools\maven.zip'}"
    
    echo Extracting Maven...
    powershell -Command "& {Expand-Archive -Path 'tools\maven.zip' -DestinationPath 'tools' -Force}"
    del tools\maven.zip
)

REM Download JavaFX SDK if not exists
if not exist "lib\javafx-sdk-17.0.2" (
    echo Downloading JavaFX SDK...
    powershell -Command "& {Invoke-WebRequest -Uri 'https://download2.gluonhq.com/openjfx/17.0.2/openjfx-17.0.2_windows-x64_bin-sdk.zip' -OutFile 'lib\javafx-sdk.zip'}"
    
    echo Extracting JavaFX SDK...
    powershell -Command "& {Expand-Archive -Path 'lib\javafx-sdk.zip' -DestinationPath 'lib' -Force}"
    del lib\javafx-sdk.zip
)

REM Set environment variables
set JAVA_HOME=C:\Program Files\Java\jdk-22
set PATH=%JAVA_HOME%\bin;%~dp0tools\apache-maven-3.9.6\bin;%PATH%
set JAVAFX_PATH=%~dp0lib\javafx-sdk-17.0.2\lib

echo Environment setup complete!
echo.
echo Now you can run the application using: run_lms.bat
pause 