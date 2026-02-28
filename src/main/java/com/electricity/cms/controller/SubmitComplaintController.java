package com.electricity.cms.controller;

import com.electricity.cms.model.Complaint;
import com.electricity.cms.model.Consumer;
import com.electricity.cms.model.User;
import com.electricity.cms.repository.ComplaintRepository;
import com.electricity.cms.repository.ConsumerRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SubmitComplaintController {

    private static final Logger LOGGER =
            Logger.getLogger(SubmitComplaintController.class.getName());

    // ─── Header ─────────────────────────────────────────
    @FXML private Label usernameLabel;

    // ─── Form Fields ────────────────────────────────────
    @FXML private ComboBox<String> complaintTypeCombo;
    @FXML private TextField        addressField;
    @FXML private TextArea         descriptionArea;
    @FXML private Label            attachedFileLabel;
    @FXML private Label            statusLabel;

    // ─── State ──────────────────────────────────────────
    private User   current_user;
    private byte[] evidence_bytes;

    private final ComplaintRepository complaint_repo = new ComplaintRepository();
    private final ConsumerRepository  consumer_repo  = new ConsumerRepository();

    // ─── Complaint Type Options ────────────────────────
    private static final List<String> COMPLAINT_TYPES = List.of(
            "Power Outage",
            "Low Voltage",
            "High Voltage",
            "Billing Issue",
            "Faulty Meter",
            "Transformer Fault",
            "Wiring / Line Fault",
            "Streetlight Issue",
            "Other"
    );

    // ─── Initialise ─────────────────────────────────────
    @FXML
    public void initialize() {
        complaintTypeCombo.setItems(
                FXCollections.observableArrayList(COMPLAINT_TYPES)
        );
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    /**
     * Inject the authenticated user. Called by the previous controller before scene swap.
     */
    public void set_user(User user) {
        if (user == null) {
            LOGGER.warning("[SubmitComplaint] set_user called with null user.");
            return;
        }
        this.current_user = user;
        String name = (user.getPerson() != null &&
                user.getPerson().getFullName() != null)
                ? user.getPerson().getFullName()
                : user.getEmail();
        usernameLabel.setText("👤 " + name);
    }

    // ─── Attach Evidence ───────────────────────────────
    @FXML
    private void handleAttachEvidence() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Evidence File");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(
                        "Image / PDF Files",
                        "*.png", "*.jpg", "*.jpeg", "*.pdf"
                ),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        Stage stage = (Stage) usernameLabel.getScene().getWindow();
        File selected_file = chooser.showOpenDialog(stage);
        if (selected_file != null) {
            try {
                evidence_bytes = Files.readAllBytes(selected_file.toPath());
                attachedFileLabel.setText(selected_file.getName());
                attachedFileLabel.setStyle(
                        "-fx-text-fill: #2e7d32; -fx-font-size: 12px;"
                );
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not read evidence file", e);
                show_message("Could not read the selected file.", false);
            }
        }
    }

    // ─── Submit Complaint ──────────────────────────────
    @FXML
    private void handleSubmitComplaint() {
        if (!validate_form()) return;
        try {
            Complaint complaint = build_complaint();
            complaint_repo.save(complaint);
            show_message(
                    "✔ Complaint submitted successfully! ID: "
                            + complaint.getId().toString().substring(0, 8).toUpperCase(),
                    true
            );
            handle_clear_after_submit();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save complaint", e);
            show_message("Error saving complaint: " + e.getMessage(), false);
        }
    }

    // ─── Navigation Handlers ──────────────────────────

    @FXML
    private void handleLogout() {
        try {
            URL url = getClass().getResource(
                    "/com/electricity/cms/fxml/login-view.fxml");
            if (url == null) throw new IOException("login-view.fxml not found");
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setTitle("ECMS — Login");
            stage.setScene(new Scene(new FXMLLoader(url).load()));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,
                    "[SubmitComplaint] Logout navigation failed", e);
        }
    }

    @FXML
    private void handleNavDashboard() {
        try {
            URL url = getClass().getResource(
                    "/com/electricity/cms/fxml/customer-dashboard.fxml");
            if (url == null) {
                show_message(
                        "customer-dashboard.fxml not found on classpath.",
                        false);
                return;
            }
            FXMLLoader loader = new FXMLLoader(url);
            Scene scene = new Scene(loader.load());
            CustomerDashboardController ctrl = loader.getController();
            ctrl.set_user(current_user);
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setTitle("ECMS — Dashboard");
            stage.setScene(scene);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE,
                    "[SubmitComplaint] Navigate to dashboard failed", e);
        }
    }

    @FXML
    private void handleNavMyComplaints() {
        // TODO: navigate to my-complaints.fxml when implemented
        show_info("My Complaints screen coming soon.");
    }

    @FXML
    private void handleNavSubmitComplaint() {
        // Already on this screen — no-op
    }

    @FXML
    private void handleNavFeedback() {
        // TODO: navigate to feedback.fxml when implemented
        show_info("Feedback screen coming soon.");
    }

    // ─── Validation ─────────────────────────────────────
    private boolean validate_form() {
        if (complaintTypeCombo.getValue() == null) {
            show_message("Please select a complaint type.", false);
            return false;
        }
        if (addressField.getText() == null ||
                addressField.getText().isBlank()) {
            show_message("Please enter the complaint address.", false);
            return false;
        }
        if (descriptionArea.getText() == null ||
                descriptionArea.getText().isBlank()) {
            show_message("Please provide a description.", false);
            return false;
        }
        return true;
    }

    // ─── Build Complaint Object ─────────────────────────
    private Complaint build_complaint() {
        Complaint complaint = new Complaint();
        if (current_user != null) {
            complaint.setPerson(current_user.getPerson());
        }
        String type    = complaintTypeCombo.getValue();
        String address = addressField.getText().trim();
        complaint.setSubject(type + " — " + address);
        complaint.setDescription(descriptionArea.getText().trim());
        complaint.setEvidence(evidence_bytes);
        complaint.setStatus(Complaint.ComplaintStatus.PENDING);
        complaint.setUpdatedAt(LocalDateTime.now());
        return complaint;
    }

    // ─── Clear After Submit ─────────────────────────────
    private void handle_clear_after_submit() {
        complaintTypeCombo.setValue(null);
        addressField.clear();
        descriptionArea.clear();
        evidence_bytes = null;
        attachedFileLabel.setText("No file selected");
        attachedFileLabel.setStyle(
                "-fx-text-fill: #9e9e9e; -fx-font-size: 12px;"
        );
    }

    // ─── Message Helpers ────────────────────────────────
    private void show_message(String message, boolean success) {
        statusLabel.setText(message);
        statusLabel.setStyle(success
                ? "-fx-text-fill: #2e7d32; -fx-font-size: 12px;" +
                  "-fx-background-color: #e8f5e9;" +
                  "-fx-background-radius: 4;" +
                  "-fx-padding: 6 10 6 10;"
                : "-fx-text-fill: #d32f2f; -fx-font-size: 12px;" +
                  "-fx-background-color: #fdecea;" +
                  "-fx-background-radius: 4;" +
                  "-fx-padding: 6 10 6 10;");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }

    private void show_info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                message, ButtonType.OK);
        alert.setTitle("ECMS");
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
