package com.electricity.cms.controller;

import com.electricity.cms.dto.DashboardStats;
import com.electricity.cms.dto.DateRange;
import com.electricity.cms.dto.UserContext;
import com.electricity.cms.model.UserRole;
import com.electricity.cms.service.ComplaintService;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

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
        loadStats(DateRange.currentMonth());
    }

    private void loadStats(DateRange range) {
        if (userContext == null) {
            return;
        }

        DashboardStats stats = complaintService.getDashboardStats(userContext.userId(), userContext.role(), range);

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
