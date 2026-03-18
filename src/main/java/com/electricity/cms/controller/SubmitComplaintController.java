package com.electricity.cms.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class SubmitComplaintController {

    // Top bar
    @FXML private Label  usernameLabel;
    @FXML private Button logoutButton;

    // Navigation
    @FXML private Button navDashboard;
    @FXML private Button navMyComplaints;
    @FXML private Button navSubmitComplaint;
    @FXML private Button navFeedback;

    // Form fields
    @FXML private ComboBox<String> complaintTypeCombo;
    @FXML private TextField        addressField;
    @FXML private TextArea         descriptionArea;
    @FXML private Label            statusLabel;
    @FXML private Button           submitButton;

    @FXML
    public void initialize() {
        complaintTypeCombo.getItems().addAll(
                "BILLING",
                "OUTAGE",
                "METER",
                "VOLTAGE",
                "OTHER"
        );
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        System.out.println("[SubmitComplaintController] initialize()");
    }

    // =============================
    // Navigation
    // =============================

    @FXML
    private void handleNavDashboard(ActionEvent event) {
        System.out.println("[SubmitComplaintController] handleNavDashboard() — switching page.");
        loadPage("/com/electricity/cms/fxml/customer-dashboard.fxml", event);
    }

    @FXML
    private void handleNavMyComplaints(ActionEvent event) {
        System.out.println("[SubmitComplaintController] handleNavMyComplaints() — switching page.");
        System.out.println("YE PAGE ABHI NAHI BANA");
        loadPage("/com/electricity/cms/fxml/my-complaints.fxml", event);
    }

    @FXML
    private void handleNavSubmitComplaint(ActionEvent event) {
        System.out.println("[SubmitComplaintController] handleNavSubmitComplaint() — already here.");
    }

    @FXML
    private void handleNavFeedback(ActionEvent event) {
        System.out.println("[SubmitComplaintController] handleNavFeedback() — switching page.");
        System.out.println("YE PAGE ABHI NAHI BANA");
        loadPage("/com/electricity/cms/fxml/feedback.fxml", event);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("[SubmitComplaintController] handleLogout() — switching page.");
        loadPage("/com/electricity/cms/fxml/login-view.fxml", event);
    }

    // =============================
    // Submit
    // =============================

    @FXML
    private void handleSubmitComplaint(ActionEvent event) {
        String type        = complaintTypeCombo.getValue();
        String address     = addressField.getText();
        String description = descriptionArea.getText();

        System.out.println("[SubmitComplaintController] handleSubmitComplaint()");

        if (type == null || address.isBlank() || description.isBlank()) {
            showStatus("Please fill all required fields.", "red");
            return;
        }

        showStatus("Complaint submitted successfully!", "green");
        System.out.println("[SubmitComplaintController] Complaint submitted — type: " + type);

        complaintTypeCombo.setValue(null);
        addressField.clear();
        descriptionArea.clear();
    }

    // =============================
    // Utility
    // =============================

    private void showStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + color + ";");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private void loadPage(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
