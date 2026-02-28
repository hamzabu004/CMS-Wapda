package com.electricity.cms.app;

import javafx.application.Application;

/**
 * Bootstrap entry point — exists to avoid the "JavaFX not on classpath"
 * error when running from a plain main() outside the JavaFX launcher.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}

