package com.electricity.cms.controller;

import java.io.IOException;

import com.electricity.cms.dto.UserContext;
import com.electricity.cms.service.AuthService;

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

public class AuthController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    @FXML
    private void login(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            showError("Username and password are required.");
            return;
        }

        try {
            UserContext userContext = authService.login(username, password);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/electricity/cms/fxml/BaseLayout.fxml"));
            Parent root = loader.load();
            MainController mainController = loader.getController();
            mainController.setUserContext(userContext);

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 760));
            stage.setResizable(true);
        } catch (IllegalArgumentException | IOException ex) {
            System.err.println("Login error: " + ex.getMessage());
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
