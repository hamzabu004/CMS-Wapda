package com.electricity.cms.controller;

import java.io.IOException;
import java.util.Optional;

import com.electricity.cms.model.Consumer;
import com.electricity.cms.model.Person;
import com.electricity.cms.model.User;
import com.electricity.cms.model.UserRole;
import com.electricity.cms.repository.ConsumerRepository;
import com.electricity.cms.repository.PersonRepository;
import com.electricity.cms.repository.UserRepository;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegistrationController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField cnicField;
    @FXML private TextField emailField;
    @FXML private Label statusLabel;

    private final ConsumerRepository consumerRepository = new ConsumerRepository();
    private final PersonRepository personRepository = new PersonRepository();
    private final UserRepository userRepository = new UserRepository();

    @FXML
    public void initialize() {
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    @FXML
    private void registerConsumer(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String cnic = cnicField.getText();
        String email = emailField.getText();

        if (cnic != null) {
            cnic = cnic.trim();
        }

        if (username == null || username.isBlank() || password == null || password.isBlank() ||
            cnic == null || cnic.isBlank() || email == null || email.isBlank()) {
            showError("All fields (Username, Password, CNIC, Email) are required.");
            return;
        }

        try {
            // Validate Username uniqueness
            if (userRepository.findByUsername(username).isPresent()) {
                showError("Username already exists.");
                return;
            }

            // CNIC must already be present in the person table.
            Optional<Person> existingPerson = personRepository.findByCnic(cnic);
            if (existingPerson.isEmpty()) {
                showError("CNIC not found in person records. Please contact support.");
                return;
            }

            if (userRepository.existsUserByCnic(cnic)) {
                showError("User with this CNIC already exists.");
                return;
            }

            Person person = existingPerson.get();

            // Verify consumer exists for this person
            Optional<Consumer> existingConsumer = consumerRepository.findByPersonId(person.getId());
            if (existingConsumer.isEmpty()) {
                showError("No consumer record found for this CNIC. Please contact support.");
                return;
            }

            // Create and persist new user account linked to this person
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setRole(UserRole.CUSTOMER);
            user.setPerson(person);
            // Note: region is NOT set for customer users (will be null)

            userRepository.save(user);

            showSuccess("Registration successful! Redirecting to login...");

            // Delayed redirect to login
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> goBackToLogin(event));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } catch (Exception ex) {
            showError("Registration failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void goBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/electricity/cms/fxml/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.sizeToScene();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #991B1B; -fx-background-color: #FEE2E2;");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: #166534; -fx-background-color: #DCFCE7;");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
}
