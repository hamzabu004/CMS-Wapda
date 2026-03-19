package com.electricity.cms.controller;

import com.electricity.cms.dto.UserContext;
import com.electricity.cms.model.ComplaintCategory;
import com.electricity.cms.model.Consumer;
import com.electricity.cms.service.ComplaintService;
import com.electricity.cms.service.ConsumerService;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class SubmitComplaintController implements UserContextAware {

    @FXML private ComboBox<ComplaintCategory> categoryCombo;
    @FXML private TextArea descriptionPlaceholder;
    @FXML private Label statusLabel;

    private final ComplaintService complaintService = new ComplaintService();
    private final ConsumerService consumerService = new ConsumerService();
    private UserContext userContext;

    @FXML
    public void initialize() {
        categoryCombo.getItems().setAll(ComplaintCategory.values());
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    @Override
    public void setUserContext(UserContext userContext) {
        this.userContext = userContext;
    }

    @FXML
    private void submitComplaint() {
        if (userContext == null) {
            showStatus("No authenticated user context.", "red");
            return;
        }

        ComplaintCategory category = categoryCombo.getValue();
        if (category == null) {
            showStatus("Please choose a complaint category.", "red");
            return;
        }

        try {
            Consumer consumer = consumerService.getConsumerByUserId(userContext.userId());
            complaintService.submitComplaint(consumer.getId(), category);
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
