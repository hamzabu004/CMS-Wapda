package com.electricity.cms.controller;
import com.electricity.cms.controller.CustomerDashboardController;
import com.electricity.cms.model.User;
import com.electricity.cms.repository.UserRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
public class LoginController {
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Label         errorLabel;
    private final UserRepository userRepository = new UserRepository();
    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        passwordField.setOnAction(event -> handleLogin());
    }
    @FXML
    private void handleLogin() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        String email    = usernameField.getText();
        String password = passwordField.getText();
        if (email == null || email.isBlank()) {
            showMessage("Email is required.", false);
            return;
        }
        if (password == null || password.isEmpty()) {
            showMessage("Password is required.", false);
            return;
        }
        try {
            Optional<User> result = userRepository.findByEmail(email.trim());
            if (result.isPresent() && password.equals(result.get().getPassword())) {
                User user = result.get();
                LOGGER.info("[Login] Success: " + email);
                navigate_to_dashboard(user);
            } else {
                passwordField.clear();
                showMessage("Invalid email or password.", false);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[Login] Unexpected error", e);
            showMessage("Error: " + e.getMessage(), false);
        }
    }
    private void navigate_to_dashboard(User user) {
        try {
            URL fxml_url = getClass().getResource("/com/electricity/cms/fxml/customer-dashboard.fxml");
            if (fxml_url == null) {
                showMessage("Dashboard screen not found. Contact support.", false);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxml_url);
            Scene scene = new Scene(loader.load());
            CustomerDashboardController ctrl = loader.getController();
            ctrl.set_user(user);
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("ECMS — Dashboard");
            stage.setScene(scene);
            stage.setResizable(true);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "[Login] Navigation to dashboard failed", e);
            showMessage("Could not open dashboard: " + e.getMessage(), false);
        }
    }

    private void showMessage(String message, boolean success) {
        errorLabel.setText(message);
        errorLabel.setStyle(success
            ? "-fx-text-fill: #2e7d32; -fx-font-size: 12px;" +
              " -fx-background-color: #e8f5e9; -fx-background-radius: 4; -fx-padding: 6 10 6 10;"
            : "-fx-text-fill: #d32f2f; -fx-font-size: 12px;" +
              " -fx-background-color: #fdecea; -fx-background-radius: 4; -fx-padding: 6 10 6 10;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
