package com.electricity.cms.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;

public class CustomerDashboardController {

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
    @FXML private TableView<?>       recentComplaintsTable;
    @FXML private TableColumn<?, ?> colComplaintId;
    @FXML private TableColumn<?, ?> colSubject;
    @FXML private TableColumn<?, ?> colStatus;
    @FXML private TableColumn<?, ?> colDate;

    @FXML
    public void initialize() {
        System.out.println("[CustomerDashboardController] initialize()");
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
        System.out.println("YE PAGE ABHI NAHI BANA");
        loadPage("/com/electricity/cms/fxml/my-complaints.fxml", event);
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
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
