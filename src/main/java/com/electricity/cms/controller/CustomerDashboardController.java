package com.electricity.cms.controller;

import com.electricity.cms.model.Complaint;
import com.electricity.cms.model.User;
import com.electricity.cms.repository.ComplaintRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for customer-dashboard.fxml.
 * Displays complaint summary stats and recent complaint history for the logged-in consumer.
 */
public class CustomerDashboardController {

    private static final Logger LOGGER = Logger.getLogger(CustomerDashboardController.class.getName());
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ─── Header ──────────────────────────────────────────────────────────────
    @FXML private Label  usernameLabel;

    // ─── Stats ───────────────────────────────────────────────────────────────
    @FXML private Label  welcomeLabel;
    @FXML private Label  totalComplaintsLabel;
    @FXML private Label  pendingLabel;
    @FXML private Label  resolvedLabel;
    @FXML private Label  inProgressLabel;

    // ─── Table ───────────────────────────────────────────────────────────────
    @FXML private TableView<ComplaintRow>     recentComplaintsTable;
    @FXML private TableColumn<ComplaintRow, String> colComplaintId;
    @FXML private TableColumn<ComplaintRow, String> colSubject;
    @FXML private TableColumn<ComplaintRow, String> colStatus;
    @FXML private TableColumn<ComplaintRow, String> colDate;

    // ─── State ───────────────────────────────────────────────────────────────
    private User   current_user;
    private final ComplaintRepository complaint_repo = new ComplaintRepository();

    // ─── Initialise ──────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        colComplaintId.setCellValueFactory(new PropertyValueFactory<>("complaint_id"));
        colSubject    .setCellValueFactory(new PropertyValueFactory<>("subject"));
        colStatus     .setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate       .setCellValueFactory(new PropertyValueFactory<>("date"));

        // Style status column cells
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String color = switch (item) {
                        case "PENDING"     -> "#f57c00";
                        case "IN_PROGRESS" -> "#1565c0";
                        case "RESOLVED"    -> "#2e7d32";
                        case "REJECTED"    -> "#c62828";
                        default            -> "#555555";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });
    }

    /**
     * Called by the previous screen (login/navigation) to inject the authenticated user.
     *
     * @param user the currently logged-in user
     */
    public void set_user(User user) {
        this.current_user = user;
        String name = (user.getPerson() != null && user.getPerson().getFullName() != null)
                ? user.getPerson().getFullName() : user.getEmail();
        usernameLabel.setText("👤 " + name);
        welcomeLabel.setText("Welcome back, " + name + "!");
        load_dashboard_data();
    }

    // ─── Data Loading ─────────────────────────────────────────────────────────

    private void load_dashboard_data() {
        if (current_user == null) return;
        try {
            List<Complaint> complaints = complaint_repo.findByUser(current_user);

            long total       = complaints.size();
            long pending     = complaints.stream().filter(c -> c.getStatus() == Complaint.ComplaintStatus.PENDING).count();
            long resolved    = complaints.stream().filter(c -> c.getStatus() == Complaint.ComplaintStatus.RESOLVED).count();
            long in_progress = complaints.stream().filter(c -> c.getStatus() == Complaint.ComplaintStatus.IN_PROGRESS).count();

            totalComplaintsLabel.setText(String.valueOf(total));
            pendingLabel        .setText(String.valueOf(pending));
            resolvedLabel       .setText(String.valueOf(resolved));
            inProgressLabel     .setText(String.valueOf(in_progress));

            ObservableList<ComplaintRow> rows = FXCollections.observableArrayList();
            complaints.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .limit(10)
                    .forEach(c -> rows.add(new ComplaintRow(
                            c.getId().toString().substring(0, 8).toUpperCase(),
                            c.getSubject() != null ? c.getSubject() : "(no subject)",
                            c.getStatus().name(),
                            c.getCreatedAt().format(DATE_FMT)
                    )));
            recentComplaintsTable.setItems(rows);

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "[CustomerDashboard] Could not load complaints: " + e.getMessage(), e);
            show_db_error();
        }
    }

    private void show_db_error() {
        totalComplaintsLabel.setText("—");
        pendingLabel        .setText("—");
        resolvedLabel       .setText("—");
        inProgressLabel     .setText("—");
    }

    // ─── Navigation Handlers ─────────────────────────────────────────────────

    @FXML
    private void handleLogout() {
        try {
            navigate_to("/com/electricity/cms/fxml/login-view.fxml", "ECMS — Login", false);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "[CustomerDashboard] Logout navigation failed", e);
        }
    }

    @FXML
    private void handleNavDashboard() {
        // already here — no-op
    }

    @FXML
    private void handleNavMyComplaints() {
        // TODO: navigate to my-complaints.fxml when implemented
        show_info("My Complaints screen coming soon.");
    }

    @FXML
    private void handleNavSubmitComplaint() {
        try {
            URL fxml_url = getClass().getResource("/com/electricity/cms/fxml/submit-complaint.fxml");
            if (fxml_url == null) {
                show_info("submit-complaint.fxml not found on classpath.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxml_url);
            Scene scene = new Scene(loader.load());
            SubmitComplaintController ctrl = loader.getController();
            ctrl.set_user(current_user);
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.setTitle("ECMS — Submit Complaint");
            stage.setScene(scene);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "[CustomerDashboard] Navigation to submit-complaint failed", e);
        }
    }

    @FXML
    private void handleNavFeedback() {
        // TODO: navigate to feedback.fxml when implemented
        show_info("Feedback screen coming soon.");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void navigate_to(String fxml_path, String title, boolean pass_user) throws IOException {
        URL url = getClass().getResource(fxml_path);
        if (url == null) throw new IOException("FXML not found: " + fxml_path);
        FXMLLoader loader = new FXMLLoader(url);
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) usernameLabel.getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(scene);
    }

    private void show_info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle("ECMS");
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    // ─── Inner Row Model ─────────────────────────────────────────────────────

    /**
     * Simple JavaFX TableView row model for recent complaints.
     */
    public static class ComplaintRow {
        private final String complaint_id;
        private final String subject;
        private final String status;
        private final String date;

        public ComplaintRow(String complaint_id, String subject, String status, String date) {
            this.complaint_id = complaint_id;
            this.subject      = subject;
            this.status       = status;
            this.date         = date;
        }

        public String getComplaint_id() { return complaint_id; }
        public String getSubject()      { return subject; }
        public String getStatus()       { return status; }
        public String getDate()         { return date; }
    }
}
