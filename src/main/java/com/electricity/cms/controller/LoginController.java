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

import com.electricity.cms.dto.UserContext;
import com.electricity.cms.service.AuthService;

public class LoginController {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;

    private final AuthService authService = new AuthService();

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

        try {
            UserContext userContext = authService.login(email, password);
            System.out.println("[LoginController] Login successful for user: " + userContext.displayName());

            // Load dashboard with user context
            loadDashboard(userContext, event);

        } catch (Exception e) {
            errorLabel.setText("Invalid username or password.");
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            System.out.println("[LoginController] Login failed: " + e.getMessage());
        }
    }

    private void loadDashboard(UserContext userContext, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/electricity/cms/fxml/customer-dashboard.fxml"));
            Parent root = loader.load();

            CustomerDashboardController controller = loader.getController();
            controller.setUserContext(userContext);

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
