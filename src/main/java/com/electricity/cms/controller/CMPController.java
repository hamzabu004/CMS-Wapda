package com.electricity.cms.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.electricity.cms.dto.DateRange;
import com.electricity.cms.dto.UserContext;
import com.electricity.cms.model.Complaint;
import com.electricity.cms.model.UserRole;
import com.electricity.cms.service.ComplaintService;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class CMPController implements UserContextAware {

    public enum ViewMode { ALL, RESOLVED, UNRESOLVED, ESCALATED, ASSIGNED, QUEUE }

    @FXML private Label screenTitleLabel;
    @FXML private ToggleButton allToggle;
    @FXML private ToggleButton unresolvedToggle;
    @FXML private ToggleButton resolvedToggle;
    @FXML private ToggleButton escalatedByMeToggle;
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

    // 🔥 IMPORTANT (for loading thread in center)
    // @FXML private StackPane contentPane; // REMOVED: Should be obtained from the Scene

    private final ComplaintService complaintService = new ComplaintService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private UserContext userContext;
    private ViewMode viewMode = ViewMode.ALL;
    private boolean roleCanEscalate;

    @FXML
    public void initialize() {
        configureColumns();
        actionColumn.setVisible(false);

        // 🔥 DOUBLE CLICK TO OPEN THREAD
        complaintsTable.setRowFactory(tv -> {
            TableRow<Complaint> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openComplaintThread(row.getItem());
                }
            });

            return row;
        });
        complaintsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateEscalateButtonState());
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
    private void showEscalatedByMe() {
        setViewMode(ViewMode.ESCALATED);
    }

    @FXML
    private void escalateSelected() {
        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null || userContext == null) {
            return;
        }

        if (userContext.role() == UserRole.REPRESENTATIVE) {
            openRepresentativeEscalationDialog(selected);
            return;
        }

        complaintService.escalate(selected.getId(), userContext.userId());
        refresh();
    }

    private void openRepresentativeEscalationDialog(Complaint complaint) {
        List<ComplaintService.TechnicianEscalationOption> options = complaintService
            .getRepresentativeEscalationOptions(complaint.getId(), userContext.userId());

        if (options.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("No technician available");
            alert.setContentText("No technician found in the complaint region.");
            alert.showAndWait();
            return;
        }

        Map<String, UUID> labelToTechnicianId = options.stream().collect(Collectors.toMap(
            this::buildTechnicianOptionLabel,
            ComplaintService.TechnicianEscalationOption::technicianId,
            (left, right) -> left
        ));

        ChoiceDialog<String> dialog = new ChoiceDialog<>(labelToTechnicianId.keySet().iterator().next(), labelToTechnicianId.keySet());
        dialog.setTitle("Escalate Complaint");
        dialog.setHeaderText("Select technician from complaint region");
        dialog.setContentText("Technician:");
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Assign");

        dialog.showAndWait().ifPresent(selectedLabel -> {
            UUID technicianId = labelToTechnicianId.get(selectedLabel);
            complaintService.escalateToTechnician(complaint.getId(), userContext.userId(), technicianId);
            refresh();
        });
    }

    private String buildTechnicianOptionLabel(ComplaintService.TechnicianEscalationOption option) {
        List<UUID> unresolvedIds = option.unresolvedAssignedComplaintIds();
        String complaintList = unresolvedIds.isEmpty()
            ? "none"
            : unresolvedIds.stream()
                .limit(4)
                .map(id -> id.toString().substring(0, 8))
                .collect(Collectors.joining(", ")) + (unresolvedIds.size() > 4 ? ", ..." : "");

        return option.technicianName()
            + " | Active unresolved: " + unresolvedIds.size()
            + " | Complaints: " + complaintList;
    }

    @FXML
    private void lodgeComplaint() {
        // your existing logic
    }

    // 🔥 NEW METHOD (THREAD OPENING)
    private void openComplaintThread(Complaint complaint) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/electricity/cms/fxml/ComplaintThread.fxml"));
            Parent root = loader.load();

            ComplaintThreadController controller = loader.getController();
            controller.initData(complaint, userContext);

            // Replace center content
            StackPane contentPane = (StackPane) complaintsTable.getScene().lookup("#contentPane");
            contentPane.getChildren().clear();
            contentPane.getChildren().add(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configureColumns() {
        idColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getId().toString()));

        categoryColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategory().name()));

        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus().name()));

        lastUpdatedColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getLastUpdated().format(formatter)));

        submittedColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCreatedAt().format(formatter)));
    }

    private void applyRoleVisibility() {
        boolean isCustomer = userContext.role() == UserRole.CUSTOMER;
        roleCanEscalate =
                userContext.role() == UserRole.REPRESENTATIVE ||
                        userContext.role() == UserRole.TECHNICIAN;

        lodgeComplaintButton.setVisible(isCustomer);
        lodgeComplaintButton.setManaged(isCustomer);

        escalateButton.setVisible(roleCanEscalate);
        escalateButton.setManaged(roleCanEscalate);

        // hide queue section for technition, customer, and manager  TODO
//        actionColumn.setVisible(false);

        updateEscalateButtonState();
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
        updateEscalateButtonState();
    }

    private void updateEscalateButtonState() {
        if (userContext == null || !roleCanEscalate || viewMode == ViewMode.QUEUE) {
            escalateButton.setDisable(true);
            return;
        }

        Complaint selected = complaintsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            escalateButton.setDisable(true);
            return;
        }

        boolean alreadyEscalated = complaintService.isComplaintEscalated(selected.getId());
        escalateButton.setDisable(alreadyEscalated);
    }

    private void configureActionColumn() {
        if (viewMode == ViewMode.QUEUE &&
                userContext != null &&
                userContext.role() == UserRole.REPRESENTATIVE) {

            actionColumn.setCellFactory(col -> new TableCell<>() {
                private final Button assignButton = new Button("Assign");

                {
                    assignButton.setOnAction(event -> {
                        Complaint complaint = getTableView().getItems().get(getIndex());
                        complaintService.assignToRepresentative(
                                complaint.getId(),
                                userContext.userId()
                        );
                        refresh();
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : assignButton);
                }
            });

            actionColumn.setCellValueFactory(data ->
                    new SimpleStringProperty("Assign"));

        } else {
            actionColumn.setCellFactory(null);
        }
    }

}