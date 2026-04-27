package com.electricity.cms.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import com.electricity.cms.dto.DashboardStats;
import com.electricity.cms.dto.DateRange;
import com.electricity.cms.dto.UserContext;
import com.electricity.cms.model.UserRole;
import com.electricity.cms.service.ComplaintService;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DashboardController implements UserContextAware {

    @FXML private Label totalLabel;
    @FXML private Label resolvedLabel;
    @FXML private Label unresolvedLabel;
    @FXML private Label escalatedLabel;
    @FXML private Label freeTechniciansLabel;
    @FXML private Label assignedTechniciansLabel;
    @FXML private Label unassignedQueueLabel;
    @FXML private HBox technicianStatsRow;

    private final ComplaintService complaintService = new ComplaintService();

    private UserContext userContext;

    @Override
    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
        loadStats(DateRange.currentMonth());
    }

    @FXML
    private void loadCurrentMonth() {
        openDateRangePickerAndLoad();
    }

    private void openDateRangePickerAndLoad() {
        if (userContext == null) {
            return;
        }

        Optional<DateRange> selectedRange = showDateRangeDialog();
        selectedRange.ifPresent(this::loadStats);
    }

    private Optional<DateRange> showDateRangeDialog() {
        Dialog<DateRange> dialog = new Dialog<>();
        dialog.setTitle("Select Date Range");
        dialog.setHeaderText("Choose start and end dates for dashboard query");

        ButtonType applyButton = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButton, ButtonType.CANCEL);

        DatePicker fromPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker toPicker = new DatePicker(LocalDate.now());

        VBox container = new VBox(10,
            new Label("From"),
            fromPicker,
            new Label("To"),
            toPicker
        );
        dialog.getDialogPane().setContent(container);

        dialog.setResultConverter(buttonType -> {
            if (buttonType != applyButton) {
                return null;
            }

            LocalDate from = fromPicker.getValue();
            LocalDate to = toPicker.getValue();
            if (from == null || to == null) {
                showRangeValidationError("Both start and end dates are required.");
                return null;
            }
            if (from.isAfter(to)) {
                showRangeValidationError("Start date cannot be after end date.");
                return null;
            }

            LocalDateTime fromDateTime = from.atStartOfDay();
            LocalDateTime toDateTime = to.atTime(LocalTime.MAX);
            return new DateRange(fromDateTime, toDateTime);
        });

        return dialog.showAndWait();
    }

    private void showRangeValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Date Range");
        alert.setHeaderText("Please correct the selected dates");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadStats(DateRange range) {
        if (userContext == null) {
            return;
        }

        UUID actorUserId = userContext.userId();
        DashboardStats stats = complaintService.getDashboardStats(actorUserId, userContext.role(), range);

        totalLabel.setText(Long.toString(stats.getTotalComplaints()));
        resolvedLabel.setText(Long.toString(stats.getResolved()));
        unresolvedLabel.setText(Long.toString(stats.getUnresolved()));
        escalatedLabel.setText(Long.toString(stats.getEscalated()));
        freeTechniciansLabel.setText(Long.toString(stats.getFreeTechnicians()));
        assignedTechniciansLabel.setText(Long.toString(stats.getAssignedTechnicians()));
        unassignedQueueLabel.setText(Long.toString(stats.getPendingUnassigned()));

        boolean showTechRow = userContext.role() == UserRole.REPRESENTATIVE || userContext.role() == UserRole.MANAGER;
        technicianStatsRow.setVisible(showTechRow);
        technicianStatsRow.setManaged(showTechRow);
    }
}
