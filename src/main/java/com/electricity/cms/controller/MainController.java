package com.electricity.cms.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.electricity.cms.dto.UserContext;
import com.electricity.cms.model.UserRole;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainController {

    private static final String NAV_BUTTON_STYLE = "-fx-background-color: transparent; -fx-text-fill: #f4f4f4; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16 12 16; -fx-font-size: 13px;";
    private static final String HOVER_NAV_BUTTON_STYLE = "-fx-background-color: #3d5a47; -fx-text-fill: #f7fff8; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16 12 16; -fx-font-size: 13px;";
    private static final String ACTIVE_NAV_BUTTON_STYLE = "-fx-background-color: #4f7f61; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16 12 16; -fx-font-size: 13px; -fx-font-weight: bold;";

    @FXML private Label headerUserLabel;
    @FXML private Label breadcrumbLabel;
    @FXML private VBox sidebarContainer;
    @FXML private StackPane contentPane;

    private UserContext userContext;
    private final Map<String, Button> navButtonsByBreadcrumb = new HashMap<>();
    private String selectedBreadcrumb;

    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
        headerUserLabel.setText(userContext.displayName() + " (" + userContext.role() + ")");
        buildSidebar();
        loadDefaultScreen();
    }

    private void buildSidebar() {
        sidebarContainer.getChildren().clear();
        navButtonsByBreadcrumb.clear();
        List<NavTarget> navTargets = navTargetsForRole(userContext.role());
        for (NavTarget target : navTargets) {
            Button button = new Button(target.label());
            button.setMaxWidth(Double.MAX_VALUE);
            button.setOnAction(event -> loadScreen(target.fxmlPath(), target.breadcrumb()));
            button.setOnMouseEntered(event -> {
                if (!target.breadcrumb().equals(selectedBreadcrumb)) {
                    button.setStyle(HOVER_NAV_BUTTON_STYLE);
                }
            });
            button.setOnMouseExited(event -> {
                if (!target.breadcrumb().equals(selectedBreadcrumb)) {
                    button.setStyle(NAV_BUTTON_STYLE);
                }
            });
            button.setStyle(NAV_BUTTON_STYLE);
            sidebarContainer.getChildren().add(button);
            navButtonsByBreadcrumb.put(target.breadcrumb(), button);
        }
    }

    private List<NavTarget> navTargetsForRole(UserRole role) {
        List<NavTarget> targets = new ArrayList<>();
        targets.add(new NavTarget("Dashboard", "/com/electricity/cms/fxml/DashboardScreen.fxml", "Dashboard"));
        if (role == UserRole.CUSTOMER) {
            targets.add(new NavTarget("Submit Complaint", "/com/electricity/cms/fxml/SubmitComplaintScreen.fxml", "Submit Complaint"));
            targets.add(new NavTarget("My Complaints", "/com/electricity/cms/fxml/CMPScreen.fxml", "My Complaints"));
            return targets;
        }
        targets.add(new NavTarget("Complaints", "/com/electricity/cms/fxml/CMPScreen.fxml", "Complaints"));
        if (role == UserRole.REPRESENTATIVE || role == UserRole.TECHNICIAN) {
            targets.add(new NavTarget("Complaints Queue", "/com/electricity/cms/fxml/CMPScreen.fxml", "Complaints Queue"));
        }
        return targets;
    }

    private void loadDefaultScreen() {
        if (userContext.role() == UserRole.CUSTOMER) {
            loadScreen("/com/electricity/cms/fxml/DashboardScreen.fxml", "Dashboard");
            return;
        }
        loadScreen("/com/electricity/cms/fxml/DashboardScreen.fxml", "Dashboard");
    }

    private void loadScreen(String fxmlPath, String breadcrumb) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent screen = loader.load();
            Object controller = loader.getController();
            if (controller instanceof UserContextAware contextAware) {
                contextAware.setUserContext(userContext);
            }
            if (controller instanceof CMPController cmpController && "Complaints Queue".equals(breadcrumb)) {
                cmpController.setViewMode(CMPController.ViewMode.QUEUE);
            }
            contentPane.getChildren().setAll(screen);
            breadcrumbLabel.setText(breadcrumb);
            updateActiveNav(breadcrumb);
        } catch (IOException e) {
            Label error = new Label("Failed to load screen: " + breadcrumb + "\n" + e.getMessage());
            contentPane.getChildren().setAll(error);
        }
    }

    private void updateActiveNav(String selectedBreadcrumb) {
        this.selectedBreadcrumb = selectedBreadcrumb;
        navButtonsByBreadcrumb.forEach((breadcrumb, button) -> {
            boolean isSelected = breadcrumb.equals(selectedBreadcrumb);
            button.setStyle(isSelected ? ACTIVE_NAV_BUTTON_STYLE : NAV_BUTTON_STYLE);
        });
    }

    @FXML
    private void logout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/electricity/cms/fxml/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) contentPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setResizable(false);
        } catch (IOException e) {
            Label error = new Label("Failed to logout: " + e.getMessage());
            contentPane.getChildren().setAll(error);
        }
    }

    private record NavTarget(String label, String fxmlPath, String breadcrumb) {
    }
}
