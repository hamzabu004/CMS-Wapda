package com.electricity.cms.controller;

import java.util.List;
import java.util.UUID;

import com.electricity.cms.dto.UserContext;
import com.electricity.cms.model.ComplaintCategory;
import com.electricity.cms.model.Consumer;
import com.electricity.cms.model.Person;
import com.electricity.cms.service.ComplaintService;
import com.electricity.cms.service.ConsumerService;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

public class SubmitComplaintController implements UserContextAware {

    @FXML private VBox consumerSelectionBox;
    @FXML private ComboBox<ComplaintCategory> categoryCombo;
    @FXML private TextArea descriptionPlaceholder;
    @FXML private Label statusLabel;

    private final ComplaintService complaintService = new ComplaintService();
    private final ConsumerService consumerService = new ConsumerService();
    private UserContext userContext;
    private Consumer selectedConsumer;
    private List<Consumer> consumers;

    @FXML
    public void initialize() {
        categoryCombo.getItems().setAll(ComplaintCategory.values());
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    @Override
    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
        loadConsumers();
    }

    private void loadConsumers() {
        if (userContext == null) {
            return;
        }

        try {
            System.out.println("Loading consumers for user: " + userContext.userId());
            UUID person = consumerService.getPersonId(userContext.userId());
            System.out.println("Found person: " + person);
            consumers = consumerService.getAllConsumersByPersonID(person);

            System.out.println("Found consumers: " + consumers.size());
            displayConsumerRadioButtons();
        } catch (RuntimeException ex) {
            showStatus("Failed to load consumers: " + ex.getMessage(), "red");
        }
    }

    private void    displayConsumerRadioButtons() {
        consumerSelectionBox.getChildren().clear();
        
        if (consumers.isEmpty()) {
            Label noConsumersLabel = new Label("No consumers found for this account.");
            consumerSelectionBox.getChildren().add(noConsumersLabel);
            return;
        }

        Label selectionLabel = new Label("Select Consumer");
        selectionLabel.setStyle("-fx-font-weight: bold;");
        consumerSelectionBox.getChildren().add(selectionLabel);

        ToggleGroup group = new ToggleGroup();
        for (Consumer consumer : consumers) {
            RadioButton radioButton = new RadioButton(consumer.getMeterAddress());
            radioButton.setToggleGroup(group);
            radioButton.setUserData(consumer);
            radioButton.setStyle("-fx-font-size: 13px;");
            radioButton.setWrapText(true);

            // Add hover tooltip with consumer details
            String regionName = consumer.getRegion() != null
                ? consumer.getRegion().getRegionName()
                : "N/A";
            String tooltipText = String.format(
                "Meter: %s\nRef: %s\nType: %s\nRegion: %s",
                consumer.getMeterNumber(),
                consumer.getConsumerReference(),
                consumer.getConnectionType(),
                regionName
            );
            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setWrapText(true);
            Tooltip.install(radioButton, tooltip);

            radioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    selectedConsumer = consumer;
                }
            });

            consumerSelectionBox.getChildren().add(radioButton);
        }

        // Select first consumer by default
        if (!consumers.isEmpty()) {
            selectedConsumer = consumers.get(0);
            ToggleGroup finalGroup = group;
            finalGroup.getToggles().stream().findFirst().ifPresent(t -> ((RadioButton) t).setSelected(true));
        }
    }

    @FXML
    private void submitComplaint() {
        if (userContext == null) {
            showStatus("No authenticated user context.", "red");
            return;
        }

        if (selectedConsumer == null) {
            showStatus("Please select a consumer.", "red");
            return;
        }

        ComplaintCategory category = categoryCombo.getValue();
        if (category == null) {
            showStatus("Please choose a complaint category.", "red");
            return;
        }

        try {
            complaintService.submitComplaint(selectedConsumer.getId(), category);
            showStatus("Complaint submitted successfully.", "green");
            categoryCombo.setValue(null);
            descriptionPlaceholder.clear();
        } catch (RuntimeException ex) {
            showStatus(ex.getMessage(), "red");
        }
    }

    private void showStatus(String message, String color) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + color + ";");
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
    }
}
