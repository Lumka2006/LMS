package com.lms;

public class Launcher {
    public static void main(String[] args) {
        try {
            // Set JavaFX properties
            System.setProperty("javafx.verbose", "true");
            System.setProperty("javafx.platform", "desktop");
            
            // Start the JavaFX application
            Main.main(args);
        } catch (Exception e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
} 