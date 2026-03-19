package com.electricity.cms.app;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.electricity.cms.controller.AuthController;
import com.electricity.cms.util.DatabaseUtil;
import com.electricity.cms.util.EmailSender;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX Application entry point for the Electricity Complaint Management System.
 */
public class MainApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(MainApp.class.getName());

    @Override
    public void init() {
        // Initialise DB connection
        try {
            DatabaseUtil.initialize();
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "[MainApp] Database initialisation failed: " + e.getMessage(), e);
            // App will still start; DB errors surface on first use
        }

        // Initialise persistent SMTP connection
        try {
           // EmailSender.getInstance().initialize();
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "[MainApp] SMTP initialisation failed: " + e.getMessage(), e);
            // App continues without email capability
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        URL fxmlUrl = AuthController.class.getResource("/com/electricity/cms/fxml/login-view.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException(
                    "[MainApp] Cannot find login-view.fxml on the classpath at " +
                    "/com/electricity/cms/fxml/login-view.fxml");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Scene scene = new Scene(loader.load());
        stage.setTitle("Electricity CMS — Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseUtil.shutdown();
        EmailSender.getInstance().shutdown();
        LOGGER.info("[MainApp] Application stopped cleanly.");
    }
}

