package com.electricity.cms.controller;
import com.electricity.cms.model.User;
import com.electricity.cms.repository.UserRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
                String name = (user.getPerson() != null && user.getPerson().getFullName() != null)
                              ? user.getPerson().getFullName() : email;
                LOGGER.info("[Login] Success: " + email);
                showMessage("Welcome, " + name + "!  |  Role: " + user.getRole(), true);
            } else {
                passwordField.clear();
                showMessage("Invalid email or password.", false);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[Login] Unexpected error", e);
            showMessage("Error: " + e.getMessage(), false);
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
