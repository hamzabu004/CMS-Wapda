package com.electricity.cms.controller;

import com.electricity.cms.model.*;
import com.electricity.cms.dto.UserContext;
import com.electricity.cms.service.ComplaintMessageService;
import com.electricity.cms.repository.ComplaintRepository;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ComplaintThreadController {

    private static final DateTimeFormatter MESSAGE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

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
        bubble.getStyleClass().addAll("thread-message-bubble", isMine ? "thread-message-mine" : "thread-message-other");

        Label sender = new Label(safeText(msg.getSenderName(), "Unknown Sender"));
        sender.getStyleClass().add("thread-message-sender");

        Label role = new Label(formatRole(msg.getSenderRole()));
        role.getStyleClass().add("thread-message-role");

        Label time = new Label(msg.getCreatedAt() != null
                ? msg.getCreatedAt().format(MESSAGE_TIME_FORMATTER)
                : "Unknown time");
        time.getStyleClass().add("thread-message-time");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(sender, role, spacer, time);
        header.getStyleClass().add("thread-message-header");

        Label text = new Label(safeText(msg.getMessageText(), ""));
        text.setWrapText(true);
        text.setMaxWidth(520);
        text.getStyleClass().add("thread-message-text");

        bubble.getChildren().addAll(header, text);
        bubble.setMaxWidth(560);

        HBox container = new HBox(bubble);
        container.setAlignment(isMine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        container.getStyleClass().add("thread-message-row");

        return container;
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String formatRole(UserRole role) {
        if (role == null) {
            return "Unknown";
        }
        String normalized = role.name().toLowerCase(Locale.ROOT).replace('_', ' ');
        String[] words = normalized.split(" ");
        StringBuilder display = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (display.length() > 0) {
                display.append(' ');
            }
            display.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return display.toString();
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