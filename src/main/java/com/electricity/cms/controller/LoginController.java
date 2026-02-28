package com.electricity.cms.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        System.out.println("[LoginController] initialize()");
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String email    = usernameField.getText();
        String password = passwordField.getText();

        System.out.println("[LoginController] handleLogin() — email: " + email);

        if (email == null || email.isBlank() || password == null || password.isEmpty()) {
            errorLabel.setText("Email and password are required.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            return;
        }

        // Navigate to dashboard
        loadPage("/com/electricity/cms/fxml/customer-dashboard.fxml", event);
        System.out.println("[LoginController] Navigating to dashboard.");
    }

    // =============================
    // Utility
    // =============================

    private void loadPage(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
