package com.electricity.cms.controller;

import com.electricity.cms.model.*;
import com.electricity.cms.dto.UserContext;
import com.electricity.cms.service.ComplaintMessageService;
import com.electricity.cms.repository.ComplaintRepository;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ComplaintThreadController {

    @FXML private VBox messagesContainer;
    @FXML private TextField messageField;
    @FXML private Button sendButton;
    @FXML private ScrollPane scrollPane;
    @FXML private Button resolveButton;

    private Complaint complaint;
    private UserContext currentUser;

    private final ComplaintMessageService messageService = new ComplaintMessageService();

    public void initData(Complaint complaint, UserContext userContext) {
        this.complaint = complaint;
        this.currentUser = userContext;

        boolean isStaff = currentUser.role() != UserRole.CUSTOMER;
        resolveButton.setVisible(isStaff);
        resolveButton.setManaged(isStaff);

        loadMessages();
        updateInputState();
    }

    private void loadMessages() {
        if (complaint == null) return;

        messagesContainer.getChildren().clear();

        List<ComplaintMessage> messages =
                messageService.getMessages(complaint.getId());

        for (ComplaintMessage msg : messages) {
            messagesContainer.getChildren().add(createMessageBubble(msg));
        }

        scrollPane.setVvalue(1.0);
    }

    private HBox createMessageBubble(ComplaintMessage msg) {

        boolean isMine = msg.getSenderId().equals(currentUser.userId());

        VBox bubble = new VBox();
        bubble.setSpacing(3);

        Label name = new Label(msg.getSenderName());
        Label role = new Label(msg.getSenderRole().toString());
        Label text = new Label(msg.getMessageText());
        Label time = new Label(
                msg.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM HH:mm"))
        );

        bubble.getChildren().addAll(name, role, text, time);

        bubble.setStyle(isMine
                ? "-fx-background-color: #3a5a3c; -fx-text-fill: white; -fx-padding: 8;"
                : "-fx-background-color: #f5f0e8; -fx-padding: 8;"
        );

        HBox container = new HBox(bubble);
        container.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        return container;
    }

    @FXML
    private void handleSend() {

        String text = messageField.getText();
        if (text == null || text.trim().isEmpty()) return;

        // customer block
        if (currentUser.role() == UserRole.CUSTOMER
                && complaint.isCustomerBlocked()) {

            showAlert("Wait for reply before sending another message.");
            return;
        }

        // now matches service
        messageService.sendMessage(complaint.getId(), currentUser, text);

        messageField.clear();

        loadMessages();
        updateInputState();
    }

    @FXML
    private void handleResolve() {

        if (complaint.getStatus() == ComplaintStatus.RESOLVED) return;

        complaint.setStatus(ComplaintStatus.RESOLVED);
        complaint.setLastUpdated(LocalDateTime.now());
        complaint.setCustomerBlocked(true);

        new ComplaintRepository().update(complaint);

        showAlert("Complaint marked as RESOLVED");

        updateInputState();
    }

    private void updateInputState() {

        // fully lock after resolve
        if (complaint.getStatus() == ComplaintStatus.RESOLVED) {
            messageField.setDisable(true);
            sendButton.setDisable(true);
            return;
        }

        if (currentUser.role() == UserRole.CUSTOMER) {
            messageField.setDisable(complaint.isCustomerBlocked());
            sendButton.setDisable(complaint.isCustomerBlocked());
        } else {
            messageField.setDisable(false);
            sendButton.setDisable(false);
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}