@echo off
mvn clean compile
java --module-path target/classes --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.media,javafx.web -cp target/classes com.lms.Main
pause 