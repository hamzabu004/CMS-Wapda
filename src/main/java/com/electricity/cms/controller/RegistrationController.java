package com.electricity.cms.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import com.electricity.cms.model.ConnType;
import com.electricity.cms.model.Consumer;
import com.electricity.cms.model.Person;
import com.electricity.cms.model.Region;
import com.electricity.cms.repository.PersonRepository;
import com.electricity.cms.repository.RegionRepository;
import com.electricity.cms.repository.UserRepository;
import com.electricity.cms.service.ConsumerService;

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

    private final ConsumerService consumerService = new ConsumerService();
    private final RegionRepository regionRepository = new RegionRepository();
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

            // Validate Duplicate CNIC via Person
            Optional<Person> existingPerson = personRepository.findByCnic(cnic);
            if (existingPerson.isPresent()) {
                if (userRepository.existsUserByCnic(cnic)) {
                    showError("User with this CNIC already exists.");
                    return;
                }
            }

            // Proceed with logic
            Person person = existingPerson.orElseGet(() -> {
                Person p = new Person();
                p.setCnic(cnic);
                p.setFullName("User " + username); // Placeholder since field removed
                p.setPhoneNumber("N/A"); // Placeholder since field removed
                return p;
            });

            // Assign first Region
            Region firstRegion = regionRepository.findFirstRegion()
                .orElseThrow(() -> new IllegalStateException("No regions found in database."));

            Consumer consumer = new Consumer();
            consumer.setRegion(firstRegion);
            // dummy fields for non-null schema requirements since removed from UI
            long epoch = System.currentTimeMillis();
            consumer.setConsumerReference("REF-" + epoch);
            consumer.setMeterNumber("MTR-" + epoch);
            consumer.setConnectionType(ConnType.RESIDENT);
            consumer.setInstallationDate(LocalDate.now());
            consumer.setMeterAddress("Not Provided");

            consumerService.registerConsumer(person, consumer, username, email, password);

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
