package com.electricity.cms.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import com.electricity.cms.dto.DateRange;
import com.electricity.cms.dto.UserContext;
import com.electricity.cms.model.Complaint;
import com.electricity.cms.model.UserRole;
import com.electricity.cms.service.ComplaintService;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;

public class CMPController implements UserContextAware {

    public enum ViewMode { ALL, RESOLVED, UNRESOLVED, ESCALATED, ASSIGNED, QUEUE }

    @FXML private Label screenTitleLabel;
    @FXML private ToggleButton allToggle;
    @FXML private ToggleButton unresolvedToggle;
    @FXML private ToggleButton resolvedToggle;
    @FXML private HBox filterBar;
    @FXML private Button escalateButton;
    @FXML private Button lodgeComplaintButton;
    @FXML private TableView<Complaint> complaintsTable;
    @FXML private TableColumn<Complaint, String> idColumn;
    @FXML private TableColumn<Complaint, String> categoryColumn;
    @FXML private TableColumn<Complaint, String> statusColumn;
    @FXML private TableColumn<Complaint, String> lastUpdatedColumn;
    @FXML private TableColumn<Complaint, String> submittedColumn;
    @FXML private TableColumn<Complaint, String> actionColumn;

    private final ComplaintService complaintService = new ComplaintService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private UserContext userContext;
    private ViewMode viewMode = ViewMode.ALL;
    private boolean roleCanEscalate;

    @FXML
    public void initialize() {
        configureColumns();
        actionColumn.setVisible(false);
    }

    @Override
    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
        applyRoleVisibility();
        refresh();
    }

    public void setViewMode(ViewMode viewMode) {
        this.viewMode = viewMode;
        refresh();
    }

    @FXML
    private void showAll() {
        setViewMode(ViewMode.ALL);
    }

    @FXML
    private void showResolved() {
        setViewMode(ViewMode.RESOLVED);
    }

    @FXML
    private void showUnresolved() {
        setViewMode(ViewMode.UNRESOLVED);
    }

    @FXML
    private void escalateSelected() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null || userContext == null) {
            return;
        }
        complaintService.escalate(selected.getId(), userContext.userId());
        refresh();
    }

    @FXML
    private void lodgeComplaint() {
        // Navigation is controlled in main layout. This action is a placeholder for row/thread integration.
    }

    private void configureColumns() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId().toString()));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory().name()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().name()));
        lastUpdatedColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLastUpdated().format(formatter)));
        submittedColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatedAt().format(formatter)));
    }

    private void applyRoleVisibility() {
        boolean isCustomer = userContext.role() == UserRole.CUSTOMER;
        roleCanEscalate = userContext.role() == UserRole.REPRESENTATIVE || userContext.role() == UserRole.TECHNICIAN;
        lodgeComplaintButton.setVisible(isCustomer);
        lodgeComplaintButton.setManaged(isCustomer);
        escalateButton.setVisible(roleCanEscalate);
        escalateButton.setManaged(roleCanEscalate);
    }

    private void refresh() {
        if (userContext == null) {
            return;
        }

        String filter = switch (viewMode) {
            case RESOLVED -> "RESOLVED";
            case UNRESOLVED -> "UNRESOLVED";
            case ESCALATED -> "ESCALATED";
            case ASSIGNED -> "ASSIGNED";
            case QUEUE -> "QUEUE";
            default -> "ALL";
        };

        List<Complaint> complaints = complaintService.getFilteredComplaints(
            userContext.userId(),
            userContext.role(),
            filter,
            DateRange.currentMonth()
        );

        complaintsTable.getItems().setAll(complaints);
        boolean isQueueMode = viewMode == ViewMode.QUEUE;
        actionColumn.setVisible(isQueueMode);
        filterBar.setVisible(!isQueueMode);
        filterBar.setManaged(!isQueueMode);
        // Queue is assignment-only, so escalation action is hidden there.
        escalateButton.setVisible(!isQueueMode && roleCanEscalate);
        escalateButton.setManaged(!isQueueMode && roleCanEscalate);
        screenTitleLabel.setText(isQueueMode ? "Complaints Queue" : "Complaints");
        configureActionColumn();
    }

    private void configureActionColumn() {
        if (viewMode != ViewMode.QUEUE || userContext == null || userContext.role() != UserRole.REPRESENTATIVE) {
            actionColumn.setCellFactory(null);
            return;
        }

        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button assignButton = new Button("Assign");

            {
                assignButton.setOnAction(event -> {
                    Complaint complaint = getTableView().getItems().get(getIndex());
                    complaintService.assignToRepresentative(complaint.getId(), userContext.userId());
                    refresh();
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : assignButton);
            }
        });

        actionColumn.setCellValueFactory(data -> new SimpleStringProperty("Assign"));
    }
}
