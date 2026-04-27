package com.electricity.cms.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import com.electricity.cms.dto.UserContext;
import com.electricity.cms.model.Complaint;
import com.electricity.cms.service.ComplaintService;

public class CustomerDashboardController implements UserContextAware {

    // Header
    @FXML private Label usernameLabel;
    @FXML private Label breadcrumbLabel;
    @FXML private Button logoutButton;

    // Side nav
    @FXML private Button navDashboard;
    @FXML private Button navMyComplaints;
    @FXML private Button navSubmitComplaint;
    @FXML private Button navFeedback;

    // Stats
    @FXML private Label welcomeLabel;
    @FXML private Label totalComplaintsLabel;
    @FXML private Label pendingLabel;
    @FXML private Label resolvedLabel;
    @FXML private Label inProgressLabel;

    // Table
    @FXML private TableView<Complaint>       recentComplaintsTable;
    @FXML private TableColumn<Complaint, String> colComplaintId;
    @FXML private TableColumn<Complaint, String> colSubject;
    @FXML private TableColumn<Complaint, String> colStatus;
    @FXML private TableColumn<Complaint, String> colDate;

    private UserContext userContext;
    private final ComplaintService complaintService = new ComplaintService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        System.out.println("[CustomerDashboardController] initialize()");
        configureTableColumns();
        setupTableClickHandler();
    }

    @Override
    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
        usernameLabel.setText(userContext.displayName());
        loadDashboardData();
    }

    private void configureTableColumns() {
        colComplaintId.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getId().toString()));

        colSubject.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory().name()));

        colStatus.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().name()));

        colDate.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getCreatedAt().format(formatter)));
    }

    private void setupTableClickHandler() {
        recentComplaintsTable.setRowFactory(tv -> {
            TableRow<Complaint> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    openComplaintThread(row.getItem());
                }
            });
            return row;
        });
    }

    private void loadDashboardData() {
        if (userContext == null) return;

        UUID actorUserId = userContext.userId();

        // Load recent complaints (limit to 10 most recent)
        var complaints = complaintService.getFilteredComplaints(
            actorUserId,
            userContext.role(),
            "ALL",
            null // no date range for recent
        ).stream().limit(10).toList();

        recentComplaintsTable.getItems().setAll(complaints);

        // Load stats
        var stats = complaintService.getDashboardStats(actorUserId, userContext.role(), null);
        totalComplaintsLabel.setText(String.valueOf(stats.getTotalComplaints()));
        resolvedLabel.setText(String.valueOf(stats.getResolved()));
        pendingLabel.setText(String.valueOf(stats.getUnresolved()));
        inProgressLabel.setText("0"); // Not implemented in stats yet
    }

    private void openComplaintThread(Complaint complaint) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/electricity/cms/fxml/ComplaintThread.fxml"));
            Parent root = loader.load();

            ComplaintThreadController controller = loader.getController();
            controller.initData(complaint, userContext);

            // Replace the current scene
            Stage stage = (Stage) recentComplaintsTable.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =============================
    // Navigation
    // =============================

    @FXML
    private void handleNavDashboard(ActionEvent event) {
        System.out.println("[CustomerDashboardController] handleNavDashboard() — already here.");
    }

    @FXML
    private void handleNavMyComplaints(ActionEvent event) {
        System.out.println("[CustomerDashboardController] handleNavMyComplaints() — switching page.");
        loadPage("/com/electricity/cms/fxml/CMPScreen.fxml", event);
    }

    @FXML
    private void handleNavSubmitComplaint(ActionEvent event) {
        System.out.println("[CustomerDashboardController] handleNavSubmitComplaint() — switching page.");
        loadPage("/com/electricity/cms/fxml/submit-complaint.fxml", event);
    }

    @FXML
    private void handleNavFeedback(ActionEvent event) {
        System.out.println("[CustomerDashboardController] handleNavFeedback() — switching page.");
        System.out.println("YE PAGE ABHI NAHI BANA");
        loadPage("/com/electricity/cms/fxml/feedback.fxml", event);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("[CustomerDashboardController] handleLogout() — switching page.");
        loadPage("/com/electricity/cms/fxml/login-view.fxml", event);
    }

    // =============================
    // Utility
    // =============================

    private void loadPage(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // If loading CMP screen, set user context
            if (fxmlPath.contains("CMPScreen") && loader.getController() instanceof UserContextAware contextAware) {
                contextAware.setUserContext(userContext);
            }

            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
